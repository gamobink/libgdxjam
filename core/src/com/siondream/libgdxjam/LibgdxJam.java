package com.siondream.libgdxjam;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonDataLoader;
import com.siondream.libgdxjam.overlap.OverlapScene;
import com.siondream.libgdxjam.overlap.OverlapSceneLoader;
import com.siondream.libgdxjam.physics.Categories;
import com.siondream.libgdxjam.physics.PhysicsData;
import com.siondream.libgdxjam.physics.PhysicsDataLoader;
import com.siondream.libgdxjam.screens.Screens;
import com.siondream.libgdxjam.tweens.CameraAccessor;
import com.siondream.libgdxjam.animation.Tags;
import com.siondream.libgdxjam.animation.AnimationControl;
import com.siondream.libgdxjam.animation.AnimationControlLoader;

public class LibgdxJam extends Game {
	private Logger logger;
	
	private Stage stage;

	private OrthographicCamera uiCamera;
	private Viewport uiViewport;
	
	private AssetManager assetManager;
	private TweenManager tweenManager;
	
	private InputMultiplexer inputMultiplexer = new InputMultiplexer();
	private Categories categories;
	private Tags tags;
	
	@Override
	public void create () {
		logger = new Logger(LibgdxJam.class.getName(), Env.LOG_LEVEL);
		
		logger.info("create");
		
		Env.init(this);
		
		Box2D.init();
		
		categories = new Categories();
		tags = new Tags();
				
		assetManager = new AssetManager();
		assetManager.setLoader(
				SkeletonData.class,
				new SkeletonDataLoader(
					new InternalFileHandleResolver()
				)
			);
		assetManager.setLoader(
			OverlapScene.class,
			new OverlapSceneLoader(
				new InternalFileHandleResolver()
			)
		);
		assetManager.setLoader(
			PhysicsData.class,
			new PhysicsDataLoader(
				new InternalFileHandleResolver(),
				categories
			)
		);
		assetManager.setLoader(
			AnimationControl.class,
			new AnimationControlLoader(
				new InternalFileHandleResolver(),
				tags
			)
		);
		
		tweenManager = new TweenManager();
		Tween.registerAccessor(Camera.class, new CameraAccessor());
		
		uiCamera = new OrthographicCamera();
		uiViewport = new ExtendViewport(
			Env.MIN_UI_WIDTH,
			Env.MIN_UI_HEIGHT,
			Env.MAX_UI_WIDTH,
			Env.MAX_UI_HEIGHT,
			uiCamera
		);
		
		stage = new Stage(uiViewport);
		
		Gdx.input.setInputProcessor(inputMultiplexer);
		inputMultiplexer.addProcessor(stage);
		
		setScreen( Screens.getLoadingScreen() );
	}
	
	@Override
	public void resize(int width, int height) {
		logger.info("resize");
		getScreen().resize(width, height);
	}
	
	@Override
	public void dispose() {
		logger.info("dispose");
		tweenManager.killAll();
		getScreen().dispose();
		stage.dispose();
		assetManager.dispose();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		stage.act(deltaTime);
		tweenManager.update(deltaTime);
		getScreen().render(deltaTime);
	}
	
	public AssetManager getAssetManager() {
		return assetManager;
	}
	
	public Stage getStage() {
		return stage;
	}
	
	public InputMultiplexer getMultiplexer() {
		return inputMultiplexer;
	}
	
	public Categories getCategories() {
		return categories;
	}
	
	public Tags getTags() {
		return tags;
	}
	
	@Override
	public void setScreen (Screen screen) {
		logger.info("setting screen: " + screen);
		stage.clear();
		super.setScreen( screen );
	}
}
