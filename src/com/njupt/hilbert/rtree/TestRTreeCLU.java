package com.njupt.hilbert.rtree;

import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import edu.rit.mp.IntegerBuf;
import edu.rit.mp.ObjectBuf;
import edu.rit.mp.buf.IntegerItemBuf;

public class TestRTreeCLU {
	
	static HilbertRTree tree;
	static IntegerItemBuf userInputBuf = IntegerBuf.buffer();
	static ObjectBuf<Point> pointBuf = ObjectBuf.buffer();
	static ObjectBuf<Rectangle> recBuf = ObjectBuf.buffer();
	
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		HilbertRTreeClu.run(args);
		tree = HilbertRTreeClu.getRTree();
		System.out.println("\n" + tree.file.readNode(0));
		System.out.println("take " + (System.currentTimeMillis() - startTime) + " msec");
		System.out.println("RTree has been built successfully!\n");
		Scanner console = null;
		
		while (true) {
			if (HilbertRTreeClu.rank == 0) {
				console = new Scanner(System.in);
				int userInput = 0;
				System.out.print("What would you like to do?\n"
						+ "1) Point query\n" + "2) Range query\n" + "3) Exit\n"
						+ "Please input digit: ");
	
				try {
					userInput = console.nextInt();
					if (userInput != 1 && userInput != 2 && userInput != 3)
						throw new InputMismatchException();
					userInputBuf = new IntegerItemBuf(userInput);
					HilbertRTreeClu.world.broadcast(0, userInputBuf);
				} catch (InputMismatchException e) {
					System.out.println("Invalid input.\n");
					continue;
				} finally {
					console.nextLine();
				}
	
				System.out.println();
	
				switch (userInput) {
				case 1:
					String input;
					String inputArray[];
					int x = 0, y = 0;

					try {
//						System.out.println("Enter x and y coordinates, " +
//								"separated by a space, in range [0, 10000]: ");
						System.out.println("Enter x and y coordinates, " +
								"separated by a space: ");
						input = console.nextLine();
						inputArray = input.split(" ");
						x = Integer.valueOf(inputArray[0]);
						y = Integer.valueOf(inputArray[1]);
						if ((x < 0 || x > 10000) || (y < 0 || y > 10000)) {
							throw new InputMismatchException();
						}
					} catch (InputMismatchException e) {
						System.out.println("Invalid input.");
					} catch (Exception e) {
						System.out.println("Invalid input.");
					} finally {
						console.reset();
					}

					Point point = new Point(new int[] { x, y } );
					pointBuf = ObjectBuf.buffer(point);
					HilbertRTreeClu.world.broadcast(0, pointBuf);
					System.out.println("rank = " + HilbertRTreeClu.rank + ", point = " + point);
					System.out.println("rank = " + HilbertRTreeClu.rank + ", pointBuf = " + pointBuf.get(0));
					pointQueryInterface(point);
					break;
				case 2:
					String rangeInput;
					String rangeArray[];
					int LX = 0, LY = 0, HX = 0, HY = 0;

					try {
//						System.out.println("Enter x and y coordinates of low corner," +
//								" separated by a space, in range [0, 10000]: ");
						System.out.println("Enter x and y coordinates of high corner, " +
								"separated by a space: ");
						rangeInput = console.nextLine();
						rangeArray = rangeInput.split(" ");
						LX = Integer.valueOf(rangeArray[0]);
						LY = Integer.valueOf(rangeArray[1]);
						if ((LX < 0 || LX > 10000) || (LY < 0 || LY > 10000)) {
							throw new InputMismatchException();
						}

//						System.out.println("Enter x and y coordinates of high corner, " +
//								"separated by a space, in range [0, 10000]: ");
						System.out.println("Enter x and y coordinates of high corner, " +
								"separated by a space: ");
						rangeInput = console.nextLine();
						rangeArray = rangeInput.split(" ");
						HX = Integer.valueOf(rangeArray[0]);
						HY = Integer.valueOf(rangeArray[1]);
						if ((HX < 0 || HX > 10000) || (HY < 0 || HY > 10000)) {
							throw new InputMismatchException();
						}
					} catch (InputMismatchException e) {
						System.out.println("Invalid input.");
					} catch (Exception e) {
						System.out.println("Invalid input.");
					} finally {
						console.reset();
					}

					Rectangle rec = new Rectangle(new Point(new int[] { LX, LY }), 
							new Point(new int[] { HX, HY }));
					recBuf = ObjectBuf.buffer(rec);
					HilbertRTreeClu.world.broadcast(0, recBuf);
					System.out.println("rank = " + HilbertRTreeClu.rank + ", rec = " + rec);
					System.out.println("rank = " + HilbertRTreeClu.rank + ", recBuf = " + recBuf.get(0));
//					rangeQueryInterface(rec);
					rangeQuery(rec);
					break;
				case 3:
					System.out.println("Goodbye!");
					console.close();
					System.exit(1);
				default:
					System.out.println("Please try again.");
					break;
				}
			} else {
				HilbertRTreeClu.world.broadcast(0, userInputBuf);
				
				switch (userInputBuf.item) {
				case 1:
					HilbertRTreeClu.world.broadcast(0, pointBuf);
					System.out.println("rank = " + HilbertRTreeClu.rank + ", pointBuf = " + pointBuf.get(0));
					pointQueryInterface(pointBuf.get(0));
					break;
				case 2:
					HilbertRTreeClu.world.broadcast(0, recBuf);
					System.out.println("rank = " + HilbertRTreeClu.rank + ", recBuf = " + recBuf.get(0));
//					rangeQueryInterface(recBuf.get(0));
					rangeQuery(recBuf.get(0));
					break;
				case 3:
					System.out.println("Goodbye!");
					System.exit(1);
				default:
					System.out.println("Please try again.");
					break;
				}
			}
		}
	}
	
	public static void pointQueryInterface(Point point) {
		List<Data> queryResults = tree.nearestNeighbor(point);
		Collections.sort(queryResults);
		if (queryResults.size() == 0) {
			System.out.println("Could not find the point (" + point.getIntCoordinate(0) + ", " + point.getIntCoordinate(1)
					+ ").");
		} else {
//			System.out.println("The Point ("  + point.getIntCoordinate(0) + ", " + point.getIntCoordinate(1)
//					+ ")" + " was found in rectangles "
//					+ queryResults.size() + " times.");
			System.out.println("\nTotal rectangles found: " + queryResults.size());
//			System.out.println(queryResults);
			for (Data rectangle : queryResults)
				System.out.print(rectangle);
			System.out.println();
		}
	}

	public static void rangeQueryInterface(Rectangle rec) {
		List<HilbertRTNode> queriedRange = tree.query(rec, tree.file.readNode(0));
		if (queriedRange.isEmpty()) {
			System.out.println("Could not find any rectangles between corners ("
					+ rec.getLow().getIntCoordinate(0) + ", " + rec.getLow().getIntCoordinate(1) + ") and (" 
					+ rec.getHigh().getIntCoordinate(0) + ", " + rec.getHigh().getIntCoordinate(1) + ")");
		} else {
			System.out.println("Total rectangles found: "
					+ queriedRange.size());
			System.out.println(queriedRange);
		}
	}
	
	public static void rangeQuery(Rectangle rec) {
		List<Rectangle> queriedRectangles = tree.queryAllRectangles(rec, tree.file.readNode(0));
		if (queriedRectangles.isEmpty()) {
			System.out.println("Could not find any rectangles between corners ("
					+ rec.getLow().getIntCoordinate(0) + ", " + rec.getLow().getIntCoordinate(1) + ") and (" 
					+ rec.getHigh().getIntCoordinate(0) + ", " + rec.getHigh().getIntCoordinate(1) + ")");
		} else {
			System.out.println("\nTotal rectangles found: "
					+ queriedRectangles.size());
			for (Rectangle rectangle : queriedRectangles)
				System.out.println(rectangle);
			System.out.println();
		}
	}
}
