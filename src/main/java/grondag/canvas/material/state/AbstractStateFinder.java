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

import grondag.canvas.apiimpl.MaterialConditionImpl;
import grondag.canvas.material.property.MaterialDecal;
import grondag.canvas.material.property.MaterialDepthTest;
import grondag.canvas.material.property.MaterialFog;
import grondag.canvas.material.property.MaterialTarget;
import grondag.canvas.material.property.MaterialTextureState;
import grondag.canvas.material.property.MaterialTransparency;
import grondag.canvas.material.property.MaterialWriteMask;
import grondag.canvas.shader.MaterialShaderImpl;
import grondag.canvas.shader.MaterialShaderManager;
import grondag.canvas.shader.ProgramType;
import grondag.frex.api.material.MaterialCondition;
import grondag.frex.api.material.MaterialShader;
import grondag.frex.api.material.RenderMaterial;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;

@SuppressWarnings("unchecked")
public abstract class AbstractStateFinder<T extends AbstractStateFinder<T, V>, V extends AbstractRenderState> extends AbstractRenderStateView{
	protected AbstractStateFinder() {
		super(AbstractRenderStateView.DEFAULT_BITS);
		vertexShaderIndex = MaterialShaderManager.DEFAULT_VERTEX_INDEX;
		fragmentShaderIndex = MaterialShaderManager.DEFAULT_FRAGMENT_INDEX;
	}

	protected int vertexShaderIndex;
	protected int fragmentShaderIndex;

	public T clear() {
		bits = AbstractRenderStateView.DEFAULT_BITS;
		vertexShaderIndex = MaterialShaderManager.DEFAULT_VERTEX_INDEX;
		fragmentShaderIndex = MaterialShaderManager.DEFAULT_FRAGMENT_INDEX;
		return (T) this;
	}

	public T primitive(int primitive) {
		assert primitive <= 7;
		bits = PRIMITIVE.setValue(primitive, bits);
		return (T) this;
	}

	//		private static final Identifier EGREGIOUS_ENDERMAN_HACK = new Identifier("textures/entity/enderman/enderman.png");

	public T texture(@Nullable Identifier id) {
		final int val = id == null ? MaterialTextureState.NO_TEXTURE.index : MaterialTextureState.fromId(id).index;
		bits = TEXTURE.setValue(val, bits);

		// WIP2: put in proper material map hooks
		//			if (id != null && id.equals(EGREGIOUS_ENDERMAN_HACK)) {
		//				fragmentShader(new Identifier("canvas:shaders/wip/material/enderman.frag"));
		//			}

		return (T) this;
	}

	public T bilinear(boolean bilinear) {
		bits = BILINEAR.setValue(bilinear, bits);
		return (T) this;
	}

	public T transparency(MaterialTransparency transparency) {
		bits = TRANSPARENCY.setValue(transparency, bits);
		return (T) this;
	}

	public T depthTest(MaterialDepthTest depthTest) {
		bits = DEPTH_TEST.setValue(depthTest, bits);
		return (T) this;
	}

	public T cull(boolean cull) {
		bits = CULL.setValue(cull, bits);
		return (T) this;
	}

	public T writeMask(MaterialWriteMask writeMask) {
		bits = WRITE_MASK.setValue(writeMask, bits);
		return (T) this;
	}

	public T enableLightmap(boolean enableLightmap) {
		bits = ENABLE_LIGHTMAP.setValue(enableLightmap, bits);
		return (T) this;
	}

	public T decal(MaterialDecal decal) {
		if (decal == MaterialDecal.TRANSLUCENT) {
			decal = MaterialDecal.NONE;
			bits = DECAL_TRANSLUCENCY.setValue(true, bits);
		} else {
			bits = DECAL_TRANSLUCENCY.setValue(true, bits);
		}

		bits = DECAL.setValue(decal, bits);
		return (T) this;
	}

	public T target(MaterialTarget target) {
		bits = TARGET.setValue(target, bits);
		return (T) this;
	}

	public T lines(boolean lines) {
		bits = LINES.setValue(lines, bits);
		return (T) this;
	}

	public T fog(MaterialFog fog) {
		bits = FOG.setValue(fog, bits);
		return (T) this;
	}

	public T vertexShader(Identifier vertexSource) {
		vertexShaderIndex = MaterialShaderManager.VERTEX_INDEXER.toHandle(vertexSource);
		return (T) this;
	}

	public T fragmentShader(Identifier fragmentSource) {
		fragmentShaderIndex = MaterialShaderManager.FRAGMENT_INDEXER.toHandle(fragmentSource);
		return (T) this;
	}

	public T emissive(boolean emissive) {
		bits = EMISSIVE.setValue(emissive, bits);
		return (T) this;
	}

	public T disableDiffuse(boolean disableDiffuse) {
		bits = DISABLE_DIFFUSE.setValue(disableDiffuse, bits);
		return (T) this;
	}

	public T disableAo(boolean disableAo) {
		bits = DISABLE_AO.setValue(disableAo, bits);
		return (T) this;
	}

	public T cutout(boolean cutout) {
		bits = CUTOUT.setValue(cutout, bits);
		return (T) this;
	}

	public T unmipped(boolean unmipped) {
		bits = UNMIPPED.setValue(unmipped, bits);
		return (T) this;
	}

	/**
	 * Sets cutout threshold to low value vs default of 50%
	 */
	public T translucentCutout(boolean translucentCutout) {
		bits = TRANSLUCENT_CUTOUT.setValue(translucentCutout, bits);
		return (T) this;
	}

	/**
	 * Used in lieu of overlay texture.  Displays red blended overlay color.
	 */
	public T hurtOverlay(boolean hurtOverlay) {
		bits = HURT_OVERLAY.setValue(hurtOverlay, bits);
		return (T) this;
	}

	/**
	 * Used in lieu of overlay texture. Displays white blended overlay color.
	 */
	public T flashOverlay(boolean flashOverlay) {
		bits = FLASH_OVERLAY.setValue(flashOverlay, bits);
		return (T) this;
	}

	public T blendMode(BlendMode blendMode) {
		switch (blendMode) {
			case CUTOUT:
				transparency(MaterialTransparency.NONE);
				cutout(true);
				unmipped(true);
				translucentCutout(false);
				bits = DEFAULT_BLEND_MODE.setValue(false, bits);
				break;
			case CUTOUT_MIPPED:
				transparency(MaterialTransparency.NONE);
				cutout(true);
				unmipped(false);
				translucentCutout(false);
				bits = DEFAULT_BLEND_MODE.setValue(false, bits);
				break;
			case TRANSLUCENT:
				transparency(MaterialTransparency.TRANSLUCENT);
				cutout(true);
				unmipped(false);
				translucentCutout(true);
				bits = DEFAULT_BLEND_MODE.setValue(false, bits);
				break;
			case DEFAULT:
				transparency(MaterialTransparency.NONE);
				cutout(false);
				unmipped(false);
				translucentCutout(false);
				bits = DEFAULT_BLEND_MODE.setValue(true, bits);
				break;
			default:
			case SOLID:
				transparency(MaterialTransparency.NONE);
				cutout(false);
				unmipped(false);
				translucentCutout(false);
				bits = DEFAULT_BLEND_MODE.setValue(false, bits);
				break;
		}

		return (T) this;
	}

	public T disableColorIndex(boolean disable) {
		bits = DISABLE_COLOR_INDEX.setValue(disable, bits);
		return (T) this;
	}

	public T shader(MaterialShader shader) {
		final MaterialShaderImpl s = (MaterialShaderImpl) shader;
		vertexShaderIndex = s.vertexShaderIndex;
		fragmentShaderIndex = s.fragmentShaderIndex;
		return (T) this;
	}

	public T condition(MaterialCondition condition) {
		bits = CONDITION.setValue(((MaterialConditionImpl) condition).index, bits);
		return (T) this;
	}

	public T copyFrom(RenderMaterial material) {
		return copyFrom((V) material);
	}

	protected abstract V missing();

	public V find() {
		// WIP: need a way to ensure only one translucent buffer/render state per target
		bits = SHADER.setValue(MaterialShaderManager.INSTANCE.find(vertexShaderIndex,fragmentShaderIndex, TRANSPARENCY.getValue(bits) == MaterialTransparency.TRANSLUCENT ? ProgramType.MATERIAL_VERTEX_LOGIC : ProgramType.MATERIAL_UNIFORM_LOGIC).index, bits);
		return findInner();
	}

	public V fromBits(long bits) {
		this.bits = bits;
		return findInner();
	}

	protected abstract V findInner();

	public T copyFrom(V template) {
		bits = template.bits;
		final MaterialShaderImpl shader = MaterialShaderManager.INSTANCE.get(SHADER.getValue(bits));
		vertexShaderIndex = shader.vertexShaderIndex;
		fragmentShaderIndex = shader.fragmentShaderIndex;
		return (T) this;
	}
}