package noMoreDarlings;

import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
	static RobotController rc;
	static Team myTeam;
	static Team enemyTeam;
	static int myRange;
	static Random rand;
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static int buildInd = 0;
	
	public static void run(RobotController mrShatner) {
		rc = mrShatner;
		rand = new Random(rc.getID());

		myRange = rc.getType().attackRadiusSquared;
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		Direction lastDirection = null;
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		RobotInfo[] myRobots;

		while(true) {
			try {
				rc.setIndicatorString(0, "This is an indicator string.");
				rc.setIndicatorString(1, "I am a " + rc.getType());
			} catch (Exception e) {
				System.out.println("Unexpected exception");
				e.printStackTrace();
			}

			if (rc.getType() == RobotType.HQ) {
				try {
					int fate = rand.nextInt(10000);
					myRobots = rc.senseNearbyRobots(999999, myTeam);
					int numSoldiers = 0;
					int numBashers = 0;
					int numBeavers = 0;
					int numBarracks = 0;
					int numMineFactories = 0;
					int numMiners = 0;
					for (RobotInfo r : myRobots) {
						RobotType type = r.type;
						if (type == RobotType.SOLDIER) {
							numSoldiers++;
						} else if (type == RobotType.BASHER) {
							numBashers++;
						} else if (type == RobotType.BEAVER) {
							numBeavers++;
						} else if (type == RobotType.BARRACKS) {
							numBarracks++;
						} else if (type == RobotType.MINERFACTORY) {
							numMineFactories++;
						} else if (type == RobotType.MINER) {
							numMiners++;
						}
					}
					rc.broadcast(0, numBeavers);
					rc.broadcast(1, numSoldiers);
					rc.broadcast(2, numBashers);
					rc.broadcast(3, numMiners);
					rc.broadcast(99, numMineFactories);
					rc.broadcast(100, numBarracks);

					if (rc.isWeaponReady()) {
						attackSomething();
					}

					if (rc.isCoreReady() && rc.getTeamOre() >= 100 && fate < Math.pow(1.2,12-numBeavers)*10000) {
						trySpawn(directions[rand.nextInt(8)], RobotType.BEAVER);
					}
					transferSupplies();
				} catch (Exception e) {
					System.out.println("HQ Exception");
					e.printStackTrace();
				}
			}

			if (rc.getType() == RobotType.TOWER) {
				try {
					if (rc.isWeaponReady()) {
						attackSomething();
					}
				} catch (Exception e) {
					System.out.println("Tower Exception");
					e.printStackTrace();
				}
			}


			if (rc.getType() == RobotType.BASHER) {
				try {
					if(rc.isWeaponReady()) {
						attackSomething();
					}
				     else if (rc.isCoreReady()) {
						 
							tryMove(directions[rand.nextInt(8)]);
						} else {
							tryMove(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
						}
					transferSupplies();
					}
				
				 catch (Exception e) {
					System.out.println("Basher Exception");
					e.printStackTrace();
				}
			}

			if (rc.getType() == RobotType.SOLDIER) {
				try {
					if (rc.isWeaponReady()) {
						attackSomething();
					}
					if (rc.isCoreReady()) {
						int fate = rand.nextInt(1000);
						if (fate < 500) {
							tryMove(directions[rand.nextInt(8)]);
						} else {
							tryMove(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
						}
					}transferSupplies();
				} catch (Exception e) {
					System.out.println("Soldier Exception");
					e.printStackTrace();
				}
			}
			if (rc.getType() == RobotType.MINER) {
				try {
					if (rc.isWeaponReady() ) {
						attackSomething();
					}
					if (rc.isCoreReady() ) {
						double oreHere = rc.senseOre(rc.getLocation());
						double oreAt = 0.0;
						int dirTo = rand.nextInt(8);
						if (oreHere <= 0.01) {
							
							
							tryMove(directions[rand.nextInt(8)]);
							
						} else {
							
							rc.mine();
						}
					}
					transferSupplies();
				} catch (Exception e) {
					System.out.println("Miner Exception");
					e.printStackTrace();
				}
			}
			if (rc.getType() == RobotType.BEAVER) {
				try {
					if (rc.isWeaponReady()) {
						attackSomething();
					}
					if (rc.isCoreReady()) {
						int fate = rand.nextInt(1000);
						int maxDir = -1;
						double oreAt= 0.0;
						if (fate < 27 && rc.getTeamOre() >= 500 ) {
							for(int dr=0; dr<8; dr++) {
								double ore=rc.senseOre(rc.getLocation().add(directions[dr]));
								if(ore>oreAt) { 
									oreAt=ore;
									maxDir=dr;
								}
							}
							int buildInd = rc.readBroadcast(4);
							buildInd++;
							rc.broadcast(4,buildInd);
							int bbd = buildInd % 4;
							if (bbd == 0 )
							{
								tryBuild(directions[rand.nextInt(8)],RobotType.BARRACKS);
							} else if(bbd==1) {
								tryBuild(directions[rand.nextInt(8)],RobotType.HANDWASHSTATION);
							} else {
								tryBuild(directions[maxDir],RobotType.MINERFACTORY);
							}	
					
						} else if (fate < 600) {
							rc.mine();
						} else if (fate < 900) {
							tryMove(directions[rand.nextInt(8)]);
						} else {
							tryMove(rc.senseHQLocation().directionTo(rc.getLocation()));
						}
					}
					transferSupplies();
				} catch (Exception e) {
					System.out.println("Beaver Exception");
					e.printStackTrace();
				}
			}
			if(rc.getType() == RobotType.MINERFACTORY) {
				try {
					int fate = rand.nextInt(10);
					int numMiners = rc.readBroadcast(3);
					if( rc.isCoreReady() && rc.getTeamOre() >= 99 &&
						fate < 3 && numMiners < 100) {
							trySpawn(directions[rand.nextInt(8)], RobotType.MINER);
					}
					transferSupplies();
				} catch (Exception e) {
					System.out.println("MINERFACTORY Excception");
					e.printStackTrace();
				}
			}
			if (rc.getType() == RobotType.BARRACKS) {
				try {
					int fate = rand.nextInt(10000);

					// get information broadcasted by the HQ
					int numBeavers = rc.readBroadcast(0);
					int numSoldiers = rc.readBroadcast(1);
					int numBashers = rc.readBroadcast(2);

					if (rc.isCoreReady() && rc.getTeamOre() >= 60 && fate < Math.pow(1.2,15-numSoldiers-numBashers+numBeavers)*10000) {
						if (rc.getTeamOre() > 80 && fate % 2 == 0) {
							trySpawn(directions[rand.nextInt(8)],RobotType.BASHER);
						} else {
							trySpawn(directions[rand.nextInt(8)],RobotType.SOLDIER);
						}
					}
					transferSupplies();
					
				} catch (Exception e) {
					System.out.println("Barracks Exception");
					e.printStackTrace();
				}
			}

			rc.yield();
		}
	}

	// This method will attack an enemy in sight, if there is one
	static void attackSomething() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
		if (enemies.length > 0) {
			rc.attackLocation(enemies[0].location);
		}
	}

	// This method will attempt to move in Direction d (or as close to it as possible)
	static void tryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 5 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
		}
	}

	// This method will attempt to spawn in the given direction (or as close to it as possible)
	static void trySpawn(Direction d, RobotType type) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 8 && !rc.canSpawn(directions[(dirint+offsets[offsetIndex]+8)%8], type)) {
			offsetIndex++;
		}
		if (offsetIndex < 8) {
			rc.spawn(directions[(dirint+offsets[offsetIndex]+8)%8], type);
		}
	}

	// This method will attempt to build in the given direction (or as close to it as possible)
	static void tryBuild(Direction d, RobotType type) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 8 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 8) {
			rc.build(directions[(dirint+offsets[offsetIndex]+8)%8], type);
		}
	}
	static void transferSupplies() throws GameActionException {
		RobotInfo[] nearbyAllies = rc.senseNearbyRobots(rc.getLocation(),GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,rc.getTeam());
		double lowestSupply = rc.getSupplyLevel();
		double transferAmount = 0;
		MapLocation suppliesToThisLocation = null;
		for(RobotInfo ri:nearbyAllies){
			if(ri.supplyLevel<lowestSupply){
				lowestSupply =ri.supplyLevel;
				transferAmount = (rc.getSupplyLevel()-ri.supplyLevel)/2;
				suppliesToThisLocation = ri.location;
			}
		}
		if(suppliesToThisLocation!=null){
			rc.transferSupplies((int)transferAmount, suppliesToThisLocation);
		}
	}

	static int directionToInt(Direction d) {
		switch(d) {
			case NORTH:
				return 0;
			case NORTH_EAST:
				return 1;
			case EAST:
				return 2;
			case SOUTH_EAST:
				return 3;
			case SOUTH:
				return 4;
			case SOUTH_WEST:
				return 5;
			case WEST:
				return 6;
			case NORTH_WEST:
				return 7;
			default:
				return -1;
		}
	}
}
