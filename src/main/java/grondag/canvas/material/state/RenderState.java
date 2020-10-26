/*
 * Copyright 2019, 2020 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package grondag.canvas.material.state;

import com.mojang.blaze3d.systems.RenderSystem;
import grondag.canvas.Configurator;
import grondag.canvas.material.MaterialVertexFormat;
import grondag.canvas.material.property.MaterialDecal;
import grondag.canvas.material.property.MaterialTarget;
import grondag.canvas.material.property.MaterialTextureState;
import grondag.canvas.render.CanvasFrameBufferHacks;
import grondag.canvas.shader.GlProgram;
import grondag.canvas.texture.SpriteInfoTexture;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.MinecraftClient;

/**
 * Primitives with the same state have the same vertex encoding,
 * same uniform state and same GL draw state. Analogous to RenderLayer<p>
 *
 * Also serves as the key for vertex collection. Primitives with the same state
 * can share the same draw call and should be packed contiguously in the buffer.<p>
 *
 * Primitives must have the same sorting requirements, which for all but the translucent
 * collection keys means there is no sorting. Translucent primitives that require sorting
 * all belong to a small handful of collectors.<p>
 *
 * Vertex data with different state can share the same buffer and should be
 * packed in glState, uniformState order for best performance.
 */
public final class RenderState extends AbstractRenderState {
	protected RenderState(long bits) {
		super(nextIndex++, bits);
	}

	@SuppressWarnings("resource")
	public void enable() {
		if (active == this) {
			return;
		}

		if (active == null) {
			// same for all, so only do 1X
			RenderSystem.shadeModel(GL11.GL_SMOOTH);
		}

		active = this;

		target.enable();
		texture.enable(bilinear);

		// WIP: make all of these do nothing if already active
		translucency.action.run();
		depthTest.action.run();
		writeMask.action.run();
		fog.action.run();
		decal.enable();

		// NB: must be after frame-buffer target switch
		if (Configurator.enableBloom) CanvasFrameBufferHacks.startEmissiveCapture();

		if (cull) {
			RenderSystem.enableCull();
		} else {
			RenderSystem.disableCull();
		}

		if (enableLightmap) {
			MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().enable();
		} else {
			MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().disable();
		}

		if (lines) {
			RenderSystem.lineWidth(Math.max(2.5F, MinecraftClient.getInstance().getWindow().getFramebufferWidth() / 1920.0F * 2.5F));
		} else {
			RenderSystem.lineWidth(1.0F);
		}

		shader.activate(texture.atlasInfo());
	}

	public static void disable() {
		if (active == null) {
			return;
		}

		active = null;

		// NB: must be before frame-buffer target switch
		if (Configurator.enableBloom) CanvasFrameBufferHacks.endEmissiveCapture();

		MaterialVertexFormat.disableDirect();
		GlProgram.deactivate();
		RenderSystem.shadeModel(GL11.GL_FLAT);
		SpriteInfoTexture.disable();
		MaterialDecal.disable();
		MaterialTarget.disable();
		MaterialTextureState.disable();
	}

	public static final int MAX_COUNT = 4096;
	static int nextIndex = 0;
	static final RenderState[] STATES = new RenderState[MAX_COUNT];
	static final Long2ObjectOpenHashMap<RenderState> MAP = new Long2ObjectOpenHashMap<>(4096, Hash.VERY_FAST_LOAD_FACTOR);

	private static RenderState active = null;

	public static final RenderState MISSING = new RenderState(0);

	static {
		STATES[0] = MISSING;
	}

	public static RenderState fromIndex(int index) {
		return STATES[index];
	}
}