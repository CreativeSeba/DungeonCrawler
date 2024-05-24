package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import java.util.HashMap;
import java.util.Random;

public class DungeonCrawler extends ApplicationAdapter {
	private int screenWidth, screenHeight;
	private int tileSize;
	private int playerTileX, playerTileY;
	private ShapeRenderer shapeRenderer;
	private OrthographicCamera camera;
	private HashMap<String, Integer> dungeonMap;
	private Random random;
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
			// Adjust the 0.3 to change the density of walls
			dungeonMap.put(key, random.nextDouble() < 0.3 ? 1 : 0);
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
