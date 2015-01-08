package player5;

import java.util.Random;

import battlecode.common.*;

public class RobotPlayer{
	
	static Direction facing;
	static Random rand;
	static RobotController rc;
	
	public static void run(RobotController myrc){
		rc = myrc;
		rand = new Random(rc.getID());
		facing = getRandomDirection();//randomize starting direction
		while(true){
			try {
				if(rc.getType()==RobotType.HQ){
					attackEnemyZero();
					spawnUnit(RobotType.BEAVER);
				}else if(rc.getType()==RobotType.BEAVER){
					if(Clock.getRoundNum()<700){
						buildUnit(RobotType.MINERFACTORY);
					}else{
						buildUnit(RobotType.BARRACKS);
					}
					mineAndMove();
				}else if(rc.getType()==RobotType.MINER){
					mineAndMove();
				}else if(rc.getType()==RobotType.MINERFACTORY){
					spawnUnit(RobotType.MINER);
				}else if(rc.getType()==RobotType.BARRACKS){
					spawnUnit(RobotType.SOLDIER);
				}else if(rc.getType()==RobotType.TOWER){
					attackEnemyZero();
				}else if(rc.getType()==RobotType.SOLDIER){
					attackEnemyZero();
					moveAround();
				}

			} catch (GameActionException e) {
				
				e.printStackTrace();
			}
			
			rc.yield();
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
		return Direction.values()[(int)(rand.nextDouble()*8)];
	}

	private static void mineAndMove() throws GameActionException {
		if(rc.senseOre(rc.getLocation())>1){//there is ore, so try to mine
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