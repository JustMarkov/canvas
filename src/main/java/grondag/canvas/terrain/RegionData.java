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

package grondag.canvas.terrain;

import java.util.List;

import grondag.canvas.buffer.encoding.VertexCollectorImpl;
import grondag.canvas.buffer.encoding.VertexCollectorList;
import grondag.canvas.material.MaterialState;
import grondag.canvas.shader.ShaderPass;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.entity.BlockEntity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RegionData {
	public static final RegionData EMPTY = new RegionData();

	final ObjectArrayList<BlockEntity> blockEntities = new ObjectArrayList<>();
	int[] occlusionData = null;

	@Nullable
	int[] translucentState;

	public List<BlockEntity> getBlockEntities() {
		return blockEntities;
	}

	public void endBuffering(float x, float y, float z, VertexCollectorList buffers) {
		final VertexCollectorImpl buffer = buffers.getIfExists(MaterialState.getDefault(ShaderPass.TRANSLUCENT));

		if (buffer != null) {
			buffer.sortQuads(x, y, z);
			translucentState = buffer.saveState(translucentState);
		}
	}

	public int[] getOcclusionData() {
		return occlusionData;
	}

	public void complete(int[] occlusionData) {
		this.occlusionData = occlusionData;
	}
}
