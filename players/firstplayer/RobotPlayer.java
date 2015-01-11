package firstplayer;

import java.util.Random;

import battlecode.common.*;

public class RobotPlayer{
	static RobotController rc;
	static Team us;
	static Team them;
	static int range;
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	public static void run(RobotController tribbles) throws InterruptedException{
		
		rc = tribbles;
		range = rc.getType().attackRadiusSquared;
		MapLocation theirBase = rc.senseEnemyHQLocation();
		Direction lastDirection = null;
		Direction nextDirection = null;
		us = rc.getTeam();
		them = us.opponent();
		while(true){
			try {
				
			   if(rc.isCoreReady()&&rc.canSpawn(Direction.NORTH, RobotType.BEAVER)){
						rc.spawn(Direction.NORTH, RobotType.BEAVER);
					}
				
				else if(rc.getType()==RobotType.BEAVER){
					if(rc.getID()%2 ==0){
						lastDirection = Direction.NORTH_EAST;
						nextDirection = Direction.NORTH;
					} else {
						lastDirection = Direction.NORTH_WEST;
						nextDirection = Direction.NORTH;
				}
					if(rc.isCoreReady()&&rc.canMove(lastDirection)){
						rc.move(lastDirection);
					}
						else if(rc.isCoreReady()&&rc.canMove(nextDirection)){
							rc.move(nextDirection);
						}
						else {
							rc.mine();
						
					}
				}}

			 catch (GameActionException e) {
				
				e.printStackTrace();
			}
			
			rc.yield();
		}
	}
		
	static void attackSomething() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(range, them);
		if (enemies.length > 0) {
			rc.attackLocation(enemies[0].location);
		}
	}

	}
	

