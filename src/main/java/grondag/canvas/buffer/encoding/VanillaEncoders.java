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

package grondag.canvas.buffer.encoding;

import grondag.canvas.apiimpl.mesh.MutableQuadViewImpl;
import grondag.canvas.apiimpl.rendercontext.AbstractRenderContext;
import grondag.canvas.material.MaterialVertexFormats;

import static grondag.canvas.buffer.encoding.EncoderUtils.applyBlockLighting;
import static grondag.canvas.buffer.encoding.EncoderUtils.applyItemLighting;
import static grondag.canvas.buffer.encoding.EncoderUtils.bufferQuad1;
import static grondag.canvas.buffer.encoding.EncoderUtils.bufferQuadDirect1;
import static grondag.canvas.buffer.encoding.EncoderUtils.colorizeQuad;

public class VanillaEncoders {
	public static final VertexEncoder VANILLA_BLOCK_1 = new VertexEncoder(MaterialVertexFormats.VANILLA_BLOCKS_AND_ITEMS) {
		@Override
		public void encodeQuad(MutableQuadViewImpl quad, AbstractRenderContext context) {
			// needs to happen before offsets are applied
			applyBlockLighting(quad, context);
			colorizeQuad(quad, context);
			bufferQuad1(quad, context);
		}
	};

	public static final VertexEncoder VANILLA_TERRAIN_1 = new VanillaTerrainEncoder() {
		@Override
		public void encodeQuad(MutableQuadViewImpl quad, AbstractRenderContext context) {
			// needs to happen before offsets are applied
			applyBlockLighting(quad, context);
			colorizeQuad(quad, context);
			bufferQuadDirect1(quad, context);
		}
	};

	public static final VertexEncoder VANILLA_ITEM_1 = new VertexEncoder(MaterialVertexFormats.VANILLA_BLOCKS_AND_ITEMS) {
		@Override
		public void encodeQuad(MutableQuadViewImpl quad, AbstractRenderContext context) {
			colorizeQuad(quad, context);
			applyItemLighting(quad, context);
			bufferQuad1(quad, context);
		}
	};

	abstract static class VanillaTerrainEncoder extends VertexEncoder {

		VanillaTerrainEncoder() {
			super(MaterialVertexFormats.VANILLA_BLOCKS_AND_ITEMS);
		}

		@Override
		public void light(VertexCollectorImpl collector, int blockLight, int skyLight) {
			// flags disable diffuse and AO in shader - mainly meant for fluids
			// TODO: toggle/remove this when do smooth fluid lighting
			collector.addi(blockLight | (skyLight << 8) | (0b00000110 << 16));
		}
	}

}
