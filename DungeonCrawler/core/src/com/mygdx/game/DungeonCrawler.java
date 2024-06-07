package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer;

import java.util.*;

public class DungeonCrawler extends ApplicationAdapter {
	Graph graph = new Graph();
	private ArrayList<Integer> road = new ArrayList<>();
	private int screenWidth, screenHeight;
	private int tileSize;
	private int playerTileX, playerTileY;
	private ShapeRenderer shapeRenderer;
	private OrthographicCamera camera;
	private HashMap<String, Integer> dungeonMap;
	private Random random;
	private Timer.Task currentTask;
	@Override
	public void create() {
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();

		// Set your desired tile size
		tileSize = 32;

		dungeonMap = new HashMap<>();
		random = new Random();

		// Start player in the middle of the initial map area
		playerTileX = 0;
		playerTileY = 0;

		shapeRenderer = new ShapeRenderer(); // Initialize ShapeRenderer

		// Initialize camera with correct viewport
		camera = new OrthographicCamera();
		camera.setToOrtho(false, screenWidth, screenHeight);

		// Ensure initial tiles are generated
		generateSurroundingTiles(playerTileX, playerTileY, 3);
	}

	@Override
	public void resize(int width, int height) {
		screenWidth = width; // Update screen width
		screenHeight = height; // Update screen height

		// Update the camera's viewport when the window is resized
		camera.viewportWidth = screenWidth;
		camera.viewportHeight = screenHeight;
		camera.update();
	}

	@Override
	public void render() {
		// Clear screen
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Handle player movement
		handleInput();

		// Update camera position to follow player
		camera.position.set((playerTileX + 0.5f) * tileSize, (playerTileY + 0.5f) * tileSize, 0); // Center camera on player
		camera.update();

		// Render the dungeon map
		renderDungeon();
	}


	private void handleInput() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
			movePlayer(-1, 0);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
			movePlayer(1, 0);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
			movePlayer(0, 1);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
			movePlayer(0, -1);
		}
		if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
			int screenX = Gdx.input.getX();
			int screenY = Gdx.input.getY();
			Vector3 worldCoordinates = camera.unproject(new Vector3(screenX, screenY, 0));

			// Convert world coordinates to tile coordinates
			int tileX = (int) Math.floor(worldCoordinates.x / tileSize);
			int tileY = (int) Math.floor(worldCoordinates.y / tileSize);

			// Check the type of tile at the clicked position
			int clickedTile = getTile(tileX, tileY);
			if (clickedTile == 1) {
				// If the clicked tile is a wall
				System.out.println("Wall!");
			} else if (clickedTile == 0) {
				// If the clicked tile is a floor
				Node start = null;
				Node end = null;
				for (int i = 0; i < graph.nodes.size(); i++) {
					Node node = graph.nodes.get(i);
					if(node.name.equals(playerTileX+","+playerTileY)){
						start = node;
					}
					else if(node.name.equals(tileX+","+tileY)){
						end = node;
					}
				}
				System.out.println("Floor! x: " +tileX+" y: "+tileY);

				if (start == null || end == null) {
					System.out.println("Start or end node not found in the graph");
				} else {
					road = graph.shortestPath(start.id, end.id);
					Collections.reverse(road); // Reverse the road list
					for (int i = 0; i < road.size(); i++) {
						System.out.println(graph.nodes.get(road.get(i)).name);
					}

					// Cancel the current task if it exists
					if (currentTask != null) {
						currentTask.cancel();
					}

					// Schedule the task here
					currentTask = Timer.schedule(new Timer.Task() {
						@Override
						public void run() {
							if (!road.isEmpty()) {
								Node nextNode = graph.nodes.get(road.get(0));
								String[] coordinates = nextNode.name.split(",");
								playerTileX = Integer.parseInt(coordinates[0]);
								playerTileY = Integer.parseInt(coordinates[1]);
								road.remove(0);

								// Generate surrounding tiles if they haven't been generated yet
								generateSurroundingTiles(playerTileX, playerTileY, 2);
							} else {
								// Cancel the task when there are no more tiles in the path
								this.cancel();
							}
						}
					}, 0, 0.15f);
				}
			} else {
				// If the clicked tile is unexplored or out of bounds
				System.out.println("Out of bounds or unexplored area!");
			}
		}

	}

	private void movePlayer(int deltaX, int deltaY) {
		int newPlayerTileX = playerTileX + deltaX;
		int newPlayerTileY = playerTileY + deltaY;

		if (isValidMove(newPlayerTileX, newPlayerTileY)) {
			playerTileX = newPlayerTileX;
			playerTileY = newPlayerTileY;

			// Generate surrounding tiles if they haven't been generated yet
			generateSurroundingTiles(newPlayerTileX, newPlayerTileY, 2);
		}
	}

	private boolean isValidMove(int x, int y) {
		return getTile(x, y) == 0;
	}

	private void generateSurroundingTiles(int centerX, int centerY, int range) {
		for (int y = centerY - range; y <= centerY + range; y++) {
			for (int x = centerX - range; x <= centerX + range; x++) {
				generateTile(x, y);
			}
		}
	}

	private void generateTile(int x, int y) {
		String key = x + "," + y;
		if (!dungeonMap.containsKey(key)) {
			// If the tile is at (0,0), set it as a floor tile
			if (x == 0 && y == 0) {
				dungeonMap.put(key, 0);
			} else {
				// Adjust the 0.3 to change the density of walls
				dungeonMap.put(key, random.nextDouble() < 0.3 ? 1 : 0);
			}
			if(dungeonMap.get(key)==0){
				Node node = graph.addNode(key);
				for (int i = 0; i < graph.nodes.size(); i++) {
					Node checkNode = graph.nodes.get(i);
					String[] nodeCoordinates = node.name.split(",");
					String[] checkNodeCoordinates = checkNode.name.split(",");
					int nodeX = Integer.parseInt(nodeCoordinates[0]);
					int nodeY = Integer.parseInt(nodeCoordinates[1]);
					int checkNodeX = Integer.parseInt(checkNodeCoordinates[0]);
					int checkNodeY = Integer.parseInt(checkNodeCoordinates[1]);
					if((Math.abs(nodeX - checkNodeX) == 1 && nodeY == checkNodeY) ||
							(Math.abs(nodeY - checkNodeY) == 1 && nodeX == checkNodeX)){
						graph.addEdge(1, node.id, checkNode.id);
					}
				}
			}
		}
	}

	private int getTile(int x, int y) {
		String key = x + "," + y;
		return dungeonMap.getOrDefault(key, -1);
	}

	private void renderDungeon() {
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeType.Filled);

		int halfScreenWidth = screenWidth / tileSize / 2;
		int halfScreenHeight = screenHeight / tileSize / 2;

		// Render visible portion of the map
		for (int y = playerTileY - halfScreenHeight - 3; y <= playerTileY + halfScreenHeight + 3; y++) {
			for (int x = playerTileX - halfScreenWidth - 3; x <= playerTileX + halfScreenWidth + 3; x++) {
				// Skip rendering the wall at (0, 0)
				if (x == 0 && y == 0 && getTile(x, y) == 1) {
					continue;
				}

				// Render floor at (0, 0)
				if (x == 0 && y == 0) {
					shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1);
					shapeRenderer.rect(x * tileSize, y * tileSize, tileSize, tileSize);
					continue;
				}

				int tile = getTile(x, y);
				if (tile == 1) {
					// Render wall
					shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1);
				} else if (tile == 0) {
					// Render floor
					shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1);
				} else {
					// Render unexplored area
					shapeRenderer.setColor(0, 0, 0, 1);
				}
				shapeRenderer.rect(x * tileSize, y * tileSize, tileSize, tileSize);
			}
		}


		shapeRenderer.setColor(1, 0, 0, 1);
		for (Integer nodeId : road) {
			Node node = graph.nodes.get(nodeId);
			String[] coordinates = node.name.split(",");
			int x = Integer.parseInt(coordinates[0]);
			int y = Integer.parseInt(coordinates[1]);
			shapeRenderer.rect(x * tileSize, y * tileSize, tileSize, tileSize);
		}

		// Render player
		shapeRenderer.setColor(1, 1, 0, 1);
		shapeRenderer.rect(playerTileX * tileSize, playerTileY * tileSize, tileSize, tileSize);

		shapeRenderer.end();
	}



	@Override
	public void dispose() {
		shapeRenderer.dispose();
	}
}
