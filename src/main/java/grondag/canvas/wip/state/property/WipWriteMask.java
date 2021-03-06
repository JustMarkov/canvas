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

package grondag.canvas.wip.state.property;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.RenderPhase.WriteMaskState;

public enum WipWriteMask {
	COLOR(() -> {
		RenderSystem.depthMask(false);
		RenderSystem.colorMask(true, true, true, true);
	}),

	DEPTH(() -> {
		RenderSystem.depthMask(true);
		RenderSystem.colorMask(false, false, false, false);
	}),

	COLOR_DEPTH(() -> {
		RenderSystem.depthMask(true);
		RenderSystem.colorMask(true, true, true, true);
	});

	public final Runnable action;

	private WipWriteMask(Runnable action) {
		this.action = action;
	}

	public static WipWriteMask fromPhase(WriteMaskState phase) {
		if (phase == RenderPhase.COLOR_MASK) {
			return COLOR;
		} else if (phase == RenderPhase.DEPTH_MASK) {
			return DEPTH;
		} else {
			return COLOR_DEPTH;
		}
	}
}
