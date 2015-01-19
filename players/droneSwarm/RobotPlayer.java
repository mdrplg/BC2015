package droneSwarm;

import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
	public static void run(RobotController rc) {
        BaseBot myself;

        if (rc.getType() == RobotType.HQ) {
            myself = new HQ(rc);
        } else if (rc.getType() == RobotType.BEAVER) {
            myself = new Beaver(rc);
        } else if (rc.getType() == RobotType.HELIPAD) {
            myself = new Helipad(rc);
        } else if (rc.getType() == RobotType.DRONE) {
            myself = new Drone(rc);
        } else if (rc.getType() == RobotType.TOWER) {
            myself = new Tower(rc);
        }else if (rc.getType() == RobotType.MINERFACTORY) {
            myself = new MinerFactory(rc);
        }
        else if (rc.getType() == RobotType.MINER) {
            myself = new Miner(rc);
        }
        else {
            myself = new BaseBot(rc);
        }

        while (true) {
            try {
                myself.go();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

    public static class BaseBot {
        protected RobotController rc;
        protected MapLocation myHQ, theirHQ;
        protected Team myTeam, theirTeam;

        public BaseBot(RobotController rc) {
            this.rc = rc;
            this.myHQ = rc.senseHQLocation();
            this.theirHQ = rc.senseEnemyHQLocation();
            this.myTeam = rc.getTeam();
            this.theirTeam = this.myTeam.opponent();
        }

        public Direction[] getDirectionsToward(MapLocation dest) {
            Direction toDest = rc.getLocation().directionTo(dest);
            Direction[] dirs = {toDest,
		    		toDest.rotateLeft(), toDest.rotateRight(),
				toDest.rotateLeft().rotateLeft(), toDest.rotateRight().rotateRight()};

            return dirs;
        }

        public Direction getMoveDir(MapLocation dest) {
            Direction[] dirs = getDirectionsToward(dest);
            for (Direction d : dirs) {
                if (rc.canMove(d)) {
                    return d;
                }
            }
            return null;
        }

        public Direction getSpawnDirection(RobotType type) {
            Direction[] dirs = getDirectionsToward(this.theirHQ);
            for (Direction d : dirs) {
                if (rc.canSpawn(d, type)) {
                    return d;
                }
            }
            return null;
        }

        public Direction getBuildDirection(RobotType type) {
            Direction[] dirs = getDirectionsToward(this.theirHQ);
            for (Direction d : dirs) {
                if (rc.canBuild(d, type)) {
                    return d;
                }
            }
            return null;
        }

        public RobotInfo[] getAllies() {
            RobotInfo[] allies = rc.senseNearbyRobots(Integer.MAX_VALUE, myTeam);
            return allies;
        }

        public RobotInfo[] getEnemiesInAttackingRange() {
            RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, theirTeam);
            return enemies;
        }

        public void attackLeastHealthEnemy(RobotInfo[] enemies) throws GameActionException {
            if (enemies.length == 0) {
                return;
            }

            double minEnergon = Double.MAX_VALUE;
            MapLocation toAttack = null;
            for (RobotInfo info : enemies) {
                if (info.health < minEnergon) {
                    toAttack = info.location;
                    minEnergon = info.health;
                }
            }

            rc.attackLocation(toAttack);
        }
        public void transferSupplies() throws GameActionException {
        	RobotInfo[] nearbyAllies = rc.senseNearbyRobots(rc.getLocation(),GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,rc.getTeam());
        	double lowestSupply = rc.getSupplyLevel();
        	double transferAmount = 0;
        	MapLocation suppliesToThisLocation = null;
        	for(RobotInfo ri:nearbyAllies){
        		if(ri.supplyLevel<lowestSupply){
        			lowestSupply = ri.supplyLevel;
        			transferAmount = (rc.getSupplyLevel()-ri.supplyLevel)/2;
        			suppliesToThisLocation = ri.location;
        		}
        	}
        	if(suppliesToThisLocation!=null){
        		rc.transferSupplies((int)transferAmount, suppliesToThisLocation);
        	}
        }

        public void beginningOfTurn() {
            if (rc.senseEnemyHQLocation() != null) {
                this.theirHQ = rc.senseEnemyHQLocation();
            }
        }

        public void endOfTurn() {
        }

        public void go() throws GameActionException {
            beginningOfTurn();
            execute();
            endOfTurn();
        }

        public void execute() throws GameActionException {
            rc.yield();
        }
    }

    public static class HQ extends BaseBot {
        public HQ(RobotController rc) {
            super(rc);
        }

        public void execute() throws GameActionException {
            int numBeavers = rc.readBroadcast(2);
            transferSupplies();
            if (rc.isCoreReady() && rc.getTeamOre() > 100 && numBeavers < 10) {
                Direction newDir = getSpawnDirection(RobotType.BEAVER);
                if (newDir != null) {
                    rc.spawn(newDir, RobotType.BEAVER);
                    rc.broadcast(2, numBeavers + 1);
                }
            }
            MapLocation rallyPoint;
            if (Clock.getRoundNum() < 1500) {
                rallyPoint = new MapLocation( (this.myHQ.x + this.theirHQ.x) / 2,
                                              (this.myHQ.y + this.theirHQ.y) / 2);
            }
            else {
                rallyPoint = this.theirHQ;
            }
            rc.broadcast(0, rallyPoint.x);
            rc.broadcast(1, rallyPoint.y);

            rc.yield();
        }
    }

    public static class Beaver extends BaseBot {
        public Beaver(RobotController rc) {
            super(rc);
        }

        public void execute() throws GameActionException {
        	transferSupplies();
        	if (rc.isCoreReady()) {
                if (rc.getTeamOre() < 500) {
                    //mine
                    if (rc.senseOre(rc.getLocation()) > 0) {
                        rc.mine();
                    }
                    else {
                        Direction newDir = getMoveDir(this.theirHQ);

                        if (newDir != null) {
                            rc.move(newDir);
                        }
                    }
                }
                else {
                   if(Clock.getRoundNum()%100<50){
                	   Direction newDir = getBuildDirection(RobotType.HELIPAD);
                	   if (newDir != null) {
                		   rc.build(newDir, RobotType.HELIPAD);
                    }}else {
                    	Direction newDir = getBuildDirection(RobotType.MINERFACTORY);
                 	   if (newDir != null) {
                 		   rc.build(newDir, RobotType.MINERFACTORY);
                    }}
                }
            }
            
            rc.yield();
        }
    }

    public static class Helipad extends BaseBot {
        public Helipad(RobotController rc) {
            super(rc);
        }

        public void execute() throws GameActionException {
        	transferSupplies();
        	if (rc.isCoreReady() && rc.getTeamOre() > 125) {
                Direction newDir = getSpawnDirection(RobotType.DRONE);
                if (newDir != null) {
                    rc.spawn(newDir, RobotType.DRONE);
                }
            }

            rc.yield();
        }
    }
    public static class MinerFactory extends BaseBot {
        public MinerFactory(RobotController rc) {
            super(rc);
        }

        public void execute() throws GameActionException {
        	int numMiners = rc.readBroadcast(3);
        	transferSupplies();
        	if (rc.isCoreReady() && rc.getTeamOre() > 125 && numMiners < 10) {
                Direction newDir = getSpawnDirection(RobotType.MINER);
                if (newDir != null) {
                    rc.spawn(newDir, RobotType.MINER);
                    rc.broadcast(3, numMiners + 1);
                }
            }

            rc.yield();
        }
    }

    public static class Drone extends BaseBot {
        public Drone(RobotController rc) {
            super(rc);
        }

        public void execute() throws GameActionException {
            RobotInfo[] enemies = getEnemiesInAttackingRange();
            transferSupplies();
            if (enemies.length > 0) {
                //attack!
                if (rc.isWeaponReady()) {
                    attackLeastHealthEnemy(enemies);
                }
            }
            else if (rc.isCoreReady()) {
                int rallyX = rc.readBroadcast(0);
                int rallyY = rc.readBroadcast(1);
                MapLocation rallyPoint = new MapLocation(rallyX, rallyY);

                Direction newDir = getMoveDir(rallyPoint);

                if (newDir != null) {
                    rc.move(newDir);
                }
            }
            rc.yield();
        }
    }
    public static class Miner extends BaseBot {
        public Miner(RobotController rc) {
            super(rc);
        }

        public void execute() throws GameActionException {
            RobotInfo[] enemies = getEnemiesInAttackingRange();
            transferSupplies();
            if (enemies.length > 0) {
                //attack!
                if (rc.isWeaponReady()) {
                    attackLeastHealthEnemy(enemies);
                }
            }
            else if (Clock.getRoundNum()>1300&&rc.isCoreReady()) {
                int rallyX = rc.readBroadcast(0);
                int rallyY = rc.readBroadcast(1);
                MapLocation rallyPoint = new MapLocation(rallyX, rallyY);

                Direction newDir = getMoveDir(rallyPoint);

                if (newDir != null) {
                    rc.move(newDir);
                }
            }
            else if (rc.isCoreReady()) {
                if (rc.getTeamOre() < 500) {
                    //mine
                    if (rc.senseOre(rc.getLocation()) > 0) {
                        rc.mine();
                    }
                    else {
                        Direction newDir = getMoveDir(this.theirHQ);

                        if (newDir != null) {
                            rc.move(newDir);
                        }
                    }
                }
            rc.yield();
        }
    }}

    public static class Tower extends BaseBot {
        public Tower(RobotController rc) {
            super(rc);
        }

        public void execute() throws GameActionException {
        	transferSupplies();
            rc.yield();
        }
    }
}
