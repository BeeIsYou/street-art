package com.streetart.client.rendering.rollerblades;

import com.mojang.blaze3d.vertex.PoseStack;
import com.streetart.AllDataComponents;
import com.streetart.AllItems;
import com.streetart.component.RollerbladeComponent;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RollerbladeRenderer implements ArmorRenderer {
    private static final MeshDefinition MESH = createRollerbladeMesh();

    public void init() {
        for (final Item rollerblade : AllItems.ROLLERBLADES) {
            ArmorRenderer.register(this, rollerblade);
        }
    }

    @Override
    public void render(final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final ItemStack stack,
                       final HumanoidRenderState humanoidRenderState, final EquipmentSlot slot, final int light,
                       final HumanoidModel<HumanoidRenderState> contextModel
    ) {

        if (!humanoidRenderState.isBaby) { // todo: baby model
            final RollerbladeComponent rollerblade = stack.get(AllDataComponents.ROLLER_BLADES);
            if (rollerblade != null) {
                final Identifier texture = rollerblade.texture();
                final ModelPart model = MESH.getRoot().bake(32, 32);
                // this feels immensely cursed
                model.getChild("right_leg").loadPose(contextModel.rightLeg.storePose());
                model.getChild("left_leg").loadPose(contextModel.leftLeg.storePose());
                submitNodeCollector.submitModelPart(model, poseStack, RenderTypes.entityCutout(texture), humanoidRenderState.lightCoords, OverlayTexture.NO_OVERLAY, null);
            }
        }
    }

    public static MeshDefinition createRollerbladeMesh() {
        final MeshDefinition mesh = new MeshDefinition();
        final PartDefinition root = mesh.getRoot();
        final CubeDeformation noop = new CubeDeformation(0.0F);

        final CubeListBuilder cubes = CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.5F, 8.0F, -2.5F, 5.0F, 5.0F, 5.0F, noop)
                .texOffs(0, 10).addBox(-2.5F, 10.0F, -4.5F, 5.0F, 3.0F, 2.0F, noop)
                .texOffs(0, 15).addBox(-1.0F, 13.0F, -5.0F, 2.0F, 2.0F, 8.0F, noop);

        root.addOrReplaceChild("right_leg", cubes, PartPose.ZERO);
        root.addOrReplaceChild("left_leg", cubes, PartPose.ZERO);

        return mesh;
    }
}
