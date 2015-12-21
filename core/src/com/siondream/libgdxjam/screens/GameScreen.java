package com.siondream.libgdxjam.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.LayerComponent;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.ParticleComponent;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.RootComponent;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.TextureComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.ZIndexComponent;
import com.siondream.libgdxjam.ecs.systems.CameraSystem;
import com.siondream.libgdxjam.ecs.systems.LayerSystem;
import com.siondream.libgdxjam.ecs.systems.NodeRemovalSystem;
import com.siondream.libgdxjam.ecs.systems.ParticleSystem;
import com.siondream.libgdxjam.ecs.systems.PhysicsSystem;
import com.siondream.libgdxjam.ecs.systems.RenderingSystem;

public class GameScreen implements Screen, InputProcessor
{
	private Stage stage;
	private OrthographicCamera uiCamera;
	private Viewport uiViewport;
	
	private OrthographicCamera camera;
	private Viewport viewport;

	private InputMultiplexer inputMultiplexer = new InputMultiplexer();
	
	private Engine engine = new Engine();
	private RenderingSystem renderingSystem;
	private World world;

	private double accumulator;
	private double currentTime;

	private Entity root;
	private Entity ball;

	private Texture texture;
	
	public GameScreen()
	{
		stage = Env.getGame().getUIStage();
		uiCamera = (OrthographicCamera) stage.getCamera();
		uiViewport = stage.getViewport();
	}
	
	@Override
	public void show()
	{
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(
				Env.MIN_WORLD_WIDTH,
				Env.MIN_WORLD_HEIGHT,
				Env.MAX_WORLD_WIDTH,
				Env.MAX_WORLD_HEIGHT,
				camera
				);

		world = new World(Env.GRAVITY, Env.DO_SLEEP);

		PhysicsSystem physicsSystem = new PhysicsSystem(world);
		engine.addSystem(physicsSystem);

		CameraSystem cameraSystem = new CameraSystem(
				camera,
				inputMultiplexer
				);

		engine.addSystem(cameraSystem);

		ParticleSystem particleSystem = new ParticleSystem(Env.UI_TO_WORLD);
		engine.addSystem(particleSystem);

		LayerSystem layerSystem = new LayerSystem();
		engine.addSystem(layerSystem);

		renderingSystem = new RenderingSystem(
				viewport,
				uiViewport,
				stage,
				world
				);
		renderingSystem.setDebug(true);
		engine.addSystem(renderingSystem);
		renderingSystem.setProcessing(false);

		NodeRemovalSystem removalSystem = new NodeRemovalSystem(engine);
		engine.addEntityListener(
				Family.all(NodeComponent.class).get(),
				removalSystem
				);

		texture = new Texture(Gdx.files.internal("badlogic.jpg"));

		root = createRootEntity();

		Entity logo1 = createLogoEntity(root, 0.0f, 0.0f, 1.0f, MathUtils.PI * 0.25f, 1.0f, 1.0f, "first");
		Entity logo2 = createLogoEntity(root, 2.0f, -2.0f, 1.0f, MathUtils.PI * 0.5f, 1.0f, 1.0f, "first");
		Entity logo3 = createLogoEntity(root, -2.0f, -2.0f, 1.5f, MathUtils.PI, 1.0f, 1.0f, "second");
		Entity logo4 = createLogoEntity(root, 0.0f, 0.0f, 1.0f, MathUtils.PI * 1.5f, 2.0f, 1.0f, "third");
		Entity particle = createParticleEntity(root, 0.0f, 0.0f, "second");
		ball = createPhysicsEntity(root, -4.0f, 20.0f, 1.0f, MathUtils.PI * 1.5f, 1.0f, 1.0f, "third");
		Entity ground = createGround();
		
		inputMultiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(inputMultiplexer);
	}

	@Override
	public void render(float delta)
	{
		double newTime = TimeUtils.millis() / 1000.0;
		double frameTime = Math.min(newTime - currentTime, Env.MAX_STEP);
		float deltaTime = (float)frameTime;
		
		currentTime = newTime;
		accumulator += frameTime;
		
		while (accumulator >= Env.STEP) {
			engine.update(deltaTime);
			stage.act(Env.STEP);
			accumulator -= Env.STEP;
			engine.getSystem(PhysicsSystem.class).setAlpha((float)accumulator / Env.STEP);
		}
		
		renderingSystem.update(Env.STEP);
	}

	@Override
	public void resize(int width, int height)
	{
		viewport.update(width, height);
		uiViewport.update(width, height);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose()
	{
		engine.getSystem(RenderingSystem.class).dispose();
		texture.dispose();
	}
	
	private Entity createRootEntity() {
		Entity entity = new Entity();
		NodeComponent node = new NodeComponent();
		RootComponent root = new RootComponent();
		LayerComponent layer = new LayerComponent();
		
		layer.names.add("first");
		layer.names.add("second");
		layer.names.add("third");
		
		entity.add(node);
		entity.add(root);
		entity.add(layer);
		
		engine.addEntity(entity);
		
		return entity;
	}
	
	private Entity createLogoEntity(Entity parent,
									float x,
							  	  	float y,
							  	  	float scale,
							  	  	float angle,
							  	  	float width,
							  	  	float height,
							  	  	String layer) {
		Entity entity = new Entity();
		TextureComponent tex = new TextureComponent();
		TransformComponent t = new TransformComponent();
		SizeComponent size = new SizeComponent();
		NodeComponent node = new NodeComponent();
		ZIndexComponent index = new ZIndexComponent();
		
		tex.region = new TextureRegion(texture);
		t.position.x = x;
		t.position.y = y;
		t.scale = scale;
		size.width = width;
		size.height = height;
		t.angle = angle;
		index.layer = layer;
		
		node.parent = parent;
		Mappers.node.get(parent).children.add(entity);
		
		entity.add(t);
		entity.add(size);
		entity.add(tex);
		entity.add(node);
		entity.add(index);
		engine.addEntity(entity);
		
		return entity;
	}
	
	private Entity createParticleEntity(Entity parent,
										float x,
							  	  		float y,
							  	  		String layer) {
		Entity entity = new Entity();
		ParticleComponent particle = new ParticleComponent();
		TransformComponent t = new TransformComponent();
		SizeComponent size = new SizeComponent();
		NodeComponent node = new NodeComponent();
		ZIndexComponent index = new ZIndexComponent();
		
		particle.effect = new ParticleEffect();
		particle.effect.load(Gdx.files.internal("bigFire"), Gdx.files.internal("."));
		t.position.x = x;
		t.position.y = y;
		index.layer = layer;
		
		node.parent = parent;
		Mappers.node.get(parent).children.add(entity);
		
		entity.add(t);
		entity.add(size);
		entity.add(particle);
		entity.add(node);
		entity.add(index);
		engine.addEntity(entity);
		
		return entity;
	}
	
	private Entity createPhysicsEntity(Entity parent,
									   float x,
							  	  	   float y,
							  	  	   float scale,
							  	  	   float angle,
							  	  	   float width,
							  	  	   float height,
							  	  	   String layer) {
		Entity entity = new Entity();
		TextureComponent tex = new TextureComponent();
		TransformComponent t = new TransformComponent();
		SizeComponent size = new SizeComponent();
		NodeComponent node = new NodeComponent();
		ZIndexComponent index = new ZIndexComponent();
		PhysicsComponent physics = new PhysicsComponent();
		
		tex.region = new TextureRegion(texture);
		t.position.x = x;
		t.position.y = y;
		t.scale = scale;
		size.width = width;
		size.height = height;
		t.angle = angle;
		index.layer = layer;
		
		BodyDef def = new BodyDef();
		def.fixedRotation = false;
		def.position.x = x;
		def.position.y = y;
		def.type = BodyDef.BodyType.DynamicBody;
		physics.body = world.createBody(def);
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width * 0.5f, height * 0.5f);
		FixtureDef fDef = new FixtureDef();
		fDef.shape = shape;
		fDef.restitution = 0.8f;
		physics.body.createFixture(fDef);
		
		node.parent = parent;
		Mappers.node.get(parent).children.add(entity);
		
		entity.add(t);
		entity.add(size);
		entity.add(tex);
		entity.add(node);
		entity.add(index);
		entity.add(physics);
		engine.addEntity(entity);
		
		return entity;
	}
	
	private Entity createGround() {
		Entity entity = new Entity();
		PhysicsComponent physics = new PhysicsComponent();
		
		
		BodyDef def = new BodyDef();
		def.position.x = 0.0f;
		def.position.y = 0.0f;
		def.type = BodyDef.BodyType.StaticBody;
		physics.body = world.createBody(def);
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(50.0f, 0.1f);
		FixtureDef fDef = new FixtureDef();
		fDef.shape = shape;
		fDef.restitution = 0.5f;
		physics.body.createFixture(fDef);
		
		entity.add(physics);
		engine.addEntity(entity);
		
		return entity;
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.D) {
			engine.getSystem(RenderingSystem.class).toggleDebug();
			return true;
		}
		
		if (keycode == Keys.W) {
			engine.removeEntity(root);
		}
		
		if (keycode == Keys.A) {
			engine.removeEntity(ball);
		}
		
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}