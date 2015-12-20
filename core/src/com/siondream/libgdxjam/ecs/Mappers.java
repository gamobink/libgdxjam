package com.siondream.libgdxjam.ecs;

import com.badlogic.ashley.core.ComponentMapper;
import com.siondream.libgdxjam.ecs.components.LayerComponent;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.ParticleComponent;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.TextureComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.ZIndexComponent;

public class Mappers {
	public static ComponentMapper<TransformComponent> transform = ComponentMapper.getFor(TransformComponent.class);
	public static ComponentMapper<TextureComponent> texture = ComponentMapper.getFor(TextureComponent.class);
	public static ComponentMapper<SizeComponent> size = ComponentMapper.getFor(SizeComponent.class);
	public static ComponentMapper<NodeComponent> node = ComponentMapper.getFor(NodeComponent.class);
	public static ComponentMapper<ParticleComponent> particle = ComponentMapper.getFor(ParticleComponent.class);
	public static ComponentMapper<LayerComponent> layer = ComponentMapper.getFor(LayerComponent.class);
	public static ComponentMapper<ZIndexComponent> index = ComponentMapper.getFor(ZIndexComponent.class);
	public static ComponentMapper<PhysicsComponent> physics = ComponentMapper.getFor(PhysicsComponent.class);
}
