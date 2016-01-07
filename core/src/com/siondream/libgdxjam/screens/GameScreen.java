package com.siondream.libgdxjam.screens;

import box2dLight.RayHandler;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.systems.CameraSystem;
import com.siondream.libgdxjam.ecs.systems.LayerSystem;
import com.siondream.libgdxjam.ecs.systems.LightSystem;
import com.siondream.libgdxjam.ecs.systems.NodeSystem;
import com.siondream.libgdxjam.ecs.systems.ParticleSystem;
import com.siondream.libgdxjam.ecs.systems.PhysicsSystem;
import com.siondream.libgdxjam.ecs.systems.RenderingSystem;
import com.siondream.libgdxjam.ecs.systems.SpineSystem;
import com.siondream.libgdxjam.ecs.systems.agents.CCTvSystem;
import com.siondream.libgdxjam.ecs.systems.agents.PlayerSystem;
import com.siondream.libgdxjam.overlap.OverlapScene;
import com.siondream.libgdxjam.overlap.OverlapSceneLoader;
import com.siondream.libgdxjam.overlap.plugins.CCTvLoader;
import com.siondream.libgdxjam.overlap.plugins.PlayerPlugin;
import com.siondream.libgdxjam.physics.Categories;
import com.siondream.libgdxjam.progression.SceneManager;

public class GameScreen implements Screen, InputProcessor {
	private OrthographicCamera camera;
	private Viewport viewport;

	private Engine engine;

	private double accumulator;
	private double currentTime;
	
	private OverlapScene scene;
	private Logger logger = new Logger(GameScreen.class.getSimpleName(), Env.LOG_LEVEL);
	
	public GameScreen() {
		logger.info("initialize");
		
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(
			Env.MIN_WORLD_WIDTH,
			Env.MIN_WORLD_HEIGHT,
			Env.MAX_WORLD_WIDTH,
			Env.MAX_WORLD_HEIGHT,
			camera
		);
		
		setupEngine();
		
		SceneManager.init(engine);
	}
	
	@Override
	public void show() {
		logger.info("show");
		
		PhysicsSystem pysicsSystem = engine.getSystem(PhysicsSystem.class);
		World world = pysicsSystem.getWorld();
		Categories categories = pysicsSystem.getCategories();
		RayHandler rayHandler = engine.getSystem(LightSystem.class).getRayHandler();
		
		SceneManager.loadScene("MainScene", world, categories, rayHandler);
		
		addInputProcessors();
	}

	@Override
	public void render(float delta) {
		double newTime = TimeUtils.millis() / 1000.0;
		double frameTime = Math.min(newTime - currentTime, Env.MAX_STEP);
		float deltaTime = (float)frameTime;
		
		currentTime = newTime;
		accumulator += frameTime;
		
		while (accumulator >= Env.STEP) {
			engine.getSystem(PhysicsSystem.class).setAlpha(Env.STEP / (float)accumulator);
			engine.update(deltaTime);
			accumulator -= Env.STEP;
		}
		
		engine.getSystem(RenderingSystem.class).update(Env.STEP);
	}

	@Override
	public void resize(int width, int height) {
		logger.info("resize");
		viewport.update(width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
		logger.info("hide");
		
		scene.removeFromEngine(engine);
		engine.removeAllEntities();
		removeInputProcessors();
	}

	@Override
	public void dispose() {
		logger.info("dispose");
		
		for (EntitySystem system : engine.getSystems()) {
			if (system instanceof Disposable) {
				((Disposable)system).dispose();
			}
		}
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.D) {
			engine.getSystem(RenderingSystem.class).toggleDebug();
			return true;
		}
		
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
	
	private void setupEngine() {
		logger.info("initializing engine");
		engine = new Engine();
		
		PhysicsSystem physicsSystem = new PhysicsSystem(
			Env.getGame().getCategories()
		);
		CameraSystem cameraSystem = new CameraSystem(camera);
		LightSystem lightSystem = new LightSystem(physicsSystem.getWorld());
		ParticleSystem particleSystem = new ParticleSystem(Env.UI_TO_WORLD);
		LayerSystem layerSystem = new LayerSystem();
		SpineSystem spineSystem = new SpineSystem();
		CCTvSystem cctvSystem = new CCTvSystem(
			physicsSystem.getWorld()
		);
		PlayerSystem playerSystem = new PlayerSystem(physicsSystem);
		RenderingSystem renderingSystem = new RenderingSystem(
			viewport,
			cameraSystem.getFocusRectangle(),
			Env.getGame().getStage(),
			physicsSystem.getWorld(),
			lightSystem.getRayHandler()
		);

		physicsSystem.priority = 1;
		cameraSystem.priority = 2;
		lightSystem.priority = 3;
		particleSystem.priority = 4;
		layerSystem.priority = 5;
		spineSystem.priority = 6;
		cctvSystem.priority = 7;
		playerSystem.priority = 8;
		renderingSystem.priority = 9;
		
		engine.addSystem(physicsSystem);
		engine.addSystem(cameraSystem);
		engine.addSystem(lightSystem);
		engine.addSystem(particleSystem);
		engine.addSystem(layerSystem);
		engine.addSystem(spineSystem);
		engine.addSystem(renderingSystem);
		engine.addSystem(cctvSystem);
		engine.addSystem(playerSystem);
		
		engine.addEntityListener(
			Family.all(NodeComponent.class).get(),
			new NodeSystem(engine)
		);
		
		renderingSystem.setDebug(true);
		renderingSystem.setProcessing(false);
	}
	
	private void addInputProcessors() {
		logger.info("enabling engine input processors");
		InputMultiplexer inputMultiplexer = Env.getGame().getMultiplexer();
		
		inputMultiplexer.addProcessor(this);
		
		for (EntitySystem system : engine.getSystems()) {
			if (system instanceof InputProcessor) {
				inputMultiplexer.addProcessor((InputProcessor)system);
			}
		}
	}
	
	private void removeInputProcessors() {
		logger.info("disabling engine input processors");
		InputMultiplexer inputMultiplexer = Env.getGame().getMultiplexer();
		
		inputMultiplexer.removeProcessor(this);
		
		for (EntitySystem system : engine.getSystems()) {
			if (system instanceof InputProcessor) {
				inputMultiplexer.removeProcessor((InputProcessor)system);
			}
		}
	}
}
