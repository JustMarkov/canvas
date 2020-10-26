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

import grondag.canvas.material.property.MaterialDecal;
import grondag.canvas.material.property.MaterialDepthTest;
import grondag.canvas.material.property.MaterialFog;
import grondag.canvas.material.property.MaterialTarget;
import grondag.canvas.material.property.MaterialTransparency;
import grondag.canvas.material.property.MaterialWriteMask;
import grondag.canvas.mixin.AccessMultiPhaseParameters;
import grondag.canvas.mixin.AccessTexture;
import grondag.canvas.mixinterface.EntityRenderDispatcherExt;
import grondag.canvas.mixinterface.MultiPhaseExt;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.SpriteAtlasTexture;

// segregates render layer references from mod init
public final class RenderLayerHelper {
	private RenderLayerHelper() {}

	private static final ReferenceOpenHashSet<RenderLayer> EXCLUSIONS = new ReferenceOpenHashSet<>(64, Hash.VERY_FAST_LOAD_FACTOR);

	static {
		// entity shadows aren't worth
		EXCLUSIONS.add(((EntityRenderDispatcherExt) MinecraftClient.getInstance().getEntityRenderDispatcher()).canvas_shadowLayer());

		// FEAT: handle more of these with shaders
		EXCLUSIONS.add(RenderLayer.getArmorGlint());
		EXCLUSIONS.add(RenderLayer.getArmorEntityGlint());
		EXCLUSIONS.add(RenderLayer.getGlint());
		EXCLUSIONS.add(RenderLayer.getDirectGlint());
		EXCLUSIONS.add(RenderLayer.method_30676());
		EXCLUSIONS.add(RenderLayer.getEntityGlint());
		EXCLUSIONS.add(RenderLayer.getDirectEntityGlint());
		EXCLUSIONS.add(RenderLayer.getLines());
		EXCLUSIONS.add(RenderLayer.getLightning());

		ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.forEach((renderLayer) -> {
			EXCLUSIONS.add(renderLayer);
		});
	}

	public static boolean isExcluded(RenderLayer layer) {
		return EXCLUSIONS.contains(layer);
	}

	public static RenderMaterialImpl copyFromLayer(RenderLayer layer) {
		if (isExcluded(layer)) {
			return RenderMaterialImpl.MISSING;
		}

		final AccessMultiPhaseParameters params = ((MultiPhaseExt) layer).canvas_phases();
		final AccessTexture tex = (AccessTexture) params.getTexture();

		final MaterialFinderImpl finder = MaterialFinderImpl.threadLocal();

		finder.primitive(GL11.GL_QUADS);
		finder.texture(tex.getId().orElse(null));
		finder.transparency(MaterialTransparency.fromPhase(params.getTransparency()));
		finder.depthTest(MaterialDepthTest.fromPhase(params.getDepthTest()));
		finder.cull(params.getCull() == RenderPhase.ENABLE_CULLING);
		finder.writeMask(MaterialWriteMask.fromPhase(params.getWriteMaskState()));
		finder.enableLightmap(params.getLightmap() == RenderPhase.ENABLE_LIGHTMAP);
		finder.decal(MaterialDecal.fromPhase(params.getLayering()));
		finder.target(MaterialTarget.fromPhase(params.getTarget()));
		finder.lines(params.getLineWidth() != RenderPhase.FULL_LINE_WIDTH);
		finder.fog(MaterialFog.fromPhase(params.getFog()));
		finder.unmipped(!tex.getMipmap());
		finder.disableDiffuse(params.getDiffuseLighting() == RenderPhase.DISABLE_DIFFUSE_LIGHTING);
		finder.cutout(params.getAlpha() != RenderPhase.ZERO_ALPHA);
		finder.translucentCutout(params.getAlpha() == RenderPhase.ONE_TENTH_ALPHA);
		finder.disableAo(true);

		// vanilla sets these as part of draw process but we don't want special casing
		if (layer ==  RenderLayer.getSolid() || layer == RenderLayer.getCutoutMipped() || layer == RenderLayer.getCutout() || layer == RenderLayer.getTranslucent()) {
			finder.cull(true);
			finder.texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
			finder.writeMask(MaterialWriteMask.COLOR_DEPTH);
			finder.enableLightmap(true);
		}

		// WIP2: put in proper material map hooks
		final String name = ((MultiPhaseExt) layer).canvas_name();
		finder.emissive(name.equals("eyes") || name.equals("beacon_beam"));

		return finder.find();
	}

	// WIP: fix translucency sort - this doesn't seem to be used consistently and sort doesn't happen
	public static final RenderMaterialImpl TRANSLUCENT_TERRAIN = copyFromLayer(RenderLayer.getTranslucent());
}
