package test;

import java.util.Random;

import battlecode.common.*;

public class RobotPlayer{
	
	static Direction facing;
	static Random rand;
	static RobotController rc;
	static int myturn;
	public static void run(RobotController myrc){
		rc = myrc;
		rand = new Random(rc.getID());
		facing = getRandomDirection();//randomize starting direction
		myturn = getSubRoundNum();
		while(true){
			try {
				if(rc.getType()==RobotType.HQ){
					attackEnemyZero();
					if(rc.isCoreReady()) {
						spawnUnit(RobotType.BEAVER);
					}
				}else if(rc.getType()==RobotType.BEAVER){
					attackEnemyZero();
					
					int fate = rand.nextInt(2000);
					double ore = rc.getTeamOre();
					if(fate<200){
						moveAround();
					}else if(fate < 500 && ore>500){
						buildUnit(RobotType.MINERFACTORY);
						mineAndMove();
						}	
					else if(fate < 1200 && ore>300){
							buildUnit(RobotType.BARRACKS);
							moveAround();
						
						}
					else if(fate < 1700 && ore>200){
							buildUnit(RobotType.HANDWASHSTATION); //This is the last know spawn point 
							moveAround();                         // referring to round number. Same goes for below
						}                                         //near same round numbers
					
					else if(fate < 1899 && ore>200){
						spawnUnit(RobotType.TECHNOLOGYINSTITUTE);
						moveAround();
					
					}
					else {
					
					mineAndMove();
					}
				}else if(rc.getType()==RobotType.MINER){
					attackEnemyZero();
					mineAndMove();
				}else if(rc.getType()==RobotType.MINERFACTORY){
					spawnUnit(RobotType.MINER);
				}else if(rc.getType()==RobotType.BARRACKS){
					if(myturn==0){
					spawnUnit(RobotType.SOLDIER);
					}else{
						spawnUnit(RobotType.BASHER);
					}
				}else if(rc.getType()==RobotType.TOWER){
					attackEnemyZero();
				}else if(rc.getType()==RobotType.SOLDIER){
					attackEnemyZero();
					moveAround();
				}
				else if(rc.getType()==RobotType.BASHER){
				attackEnemyZero();
				moveAround();
				}
			    else if(rc.getType()==RobotType.TECHNOLOGYINSTITUTE){
			    	if(Clock.getRoundNum()>1200&&Clock.getRoundNum()<1350){
			    		spawnUnit(RobotType.TRAININGFIELD);
			    	}else{
			    		spawnUnit(RobotType.COMPUTER);
			    	}
			    }
				transferSupplies();
				
			} catch (GameActionException e) {
				
				e.printStackTrace();
			}
			
			rc.yield();
		}
		
	}

	private static void transferSupplies() throws GameActionException {
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

	private static void buildUnit(RobotType type) throws GameActionException {
		if(rc.getTeamOre()>type.oreCost){
			Direction buildDir = getRandomDirection();
			if(rc.isCoreReady()&&rc.canBuild(buildDir, type)){
				rc.build(buildDir, type);
			}
		}
	}

	private static void attackEnemyZero() throws GameActionException {
		RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(rc.getLocation(),rc.getType().attackRadiusSquared,rc.getTeam().opponent());
		if(nearbyEnemies.length>0){//there are enemies nearby
			//try to shoot at them
			//specifically, try to shoot at enemy specified by nearbyEnemies[0]
			if(rc.isWeaponReady()&&rc.canAttackLocation(nearbyEnemies[0].location)){
				rc.attackLocation(nearbyEnemies[0].location);
			}
		}
	}

	private static void spawnUnit(RobotType type) throws GameActionException {
		Direction randomDir = getRandomDirection();
		if(rc.isCoreReady()&&rc.canSpawn(randomDir, type)){
			rc.spawn(randomDir, type);
		}
	}

	private static Direction getRandomDirection() {
		return Direction.values()[rand.nextInt(8)];
	}
	private static int getSubRoundNum() {
		return Clock.getRoundNum()%2;
	}

	private static void mineAndMove() throws GameActionException {
		if(rc.senseOre(rc.getLocation())>5){//there is ore, so try to mine
			if(rc.isCoreReady()&&rc.canMine()){
				rc.mine();
			}
		}else{//no ore, so look for ore
			moveAround();
		}
	}

	private static void moveAround() throws GameActionException {
		if(rand.nextDouble()<0.05){
			if(rand.nextDouble()<0.5){
				facing = facing.rotateLeft();
			}else{
				facing = facing.rotateRight();
			}
		}
		MapLocation tileInFront = rc.getLocation().add(facing);
		
		//check that the direction in front is not a tile that can be attacked by the enemy towers
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		boolean tileInFrontSafe = true;
		for(MapLocation m: enemyTowers){
			if(m.distanceSquaredTo(tileInFront)<=RobotType.TOWER.attackRadiusSquared){
				tileInFrontSafe = false;
				break;
			}
		}

		//check that we are not facing off the edge of the map
		if(rc.senseTerrainTile(tileInFront)!=TerrainTile.NORMAL||!tileInFrontSafe){
			facing = facing.rotateLeft();
		}else{
			//try to move in the facing direction
			if(rc.isCoreReady()&&rc.canMove(facing)){
				rc.move(facing);
			}
		}
	}
	
}