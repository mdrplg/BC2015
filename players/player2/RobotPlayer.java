package player2;

import java.util.Random;

import battlecode.common.*;

public class RobotPlayer{
	
	static Direction facing;
	static Random rand;
	static RobotController rc;
	
	public static void run(RobotController myrc){
		rc = myrc;
		rand = new Random(rc.getID());
		facing = Direction.values()[(int)(rand.nextDouble()*8)];//randomize starting direction
		while(true){
			try {
				if(rc.getType()==RobotType.HQ){
					if(rc.isCoreReady()&&rc.canSpawn(Direction.NORTH, RobotType.BEAVER)){
						rc.spawn(Direction.NORTH, RobotType.BEAVER);
					}
				}else if(rc.getType()==RobotType.BEAVER){
					if(rc.senseOre(rc.getLocation())>1){//there is ore, so try to mine
						if(rc.isCoreReady()&&rc.canMine()){
							rc.mine();
						}
					}else{//no ore, so look for ore
						moveAround();
					}
				}

			} catch (GameActionException e) {
				
				e.printStackTrace();
			}
			
			rc.yield();
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
		//check that we are not facing off the edge of the map
		if(rc.senseTerrainTile(rc.getLocation().add(facing))!=TerrainTile.NORMAL){
			facing = facing.rotateLeft();
		}
		//try to move in the facing direction
		if(rc.isCoreReady()&&rc.canMove(facing)){
			rc.move(facing);
		}
	}
	
}