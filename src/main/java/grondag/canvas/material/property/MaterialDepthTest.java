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

package grondag.canvas.material.property;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.RenderPhase.DepthTest;

public enum MaterialDepthTest {
	DISABLE(() -> {
		RenderSystem.disableDepthTest();
		RenderSystem.depthFunc(GL11.GL_NEVER);
	}),

	ALWAYS(() -> {
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_ALWAYS);
	}),

	EQUAL(() -> {
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_EQUAL);
	}),

	LEQUAL(() -> {
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
	});

	private final Runnable action;

	private MaterialDepthTest(Runnable action) {
		this.action = action;
	}

	public void enable() {
		if (active != this) {
			action.run();
			active = this;
		}
	}

	public static MaterialDepthTest fromPhase(DepthTest phase) {
		if (phase == RenderPhase.ALWAYS_DEPTH_TEST) {
			return ALWAYS;
		} else if (phase == RenderPhase.EQUAL_DEPTH_TEST) {
			return EQUAL;
		} else if (phase == RenderPhase.LEQUAL_DEPTH_TEST) {
			return LEQUAL;
		} else {
			return DISABLE;
		}
	}

	private static MaterialDepthTest active = null;

	public static void disable() {
		if (active != null) {
			DISABLE.action.run();
			active = null;
		}
	}
}
