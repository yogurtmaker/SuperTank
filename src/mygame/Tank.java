/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;
import java.util.List;

public class Tank implements ActionListener {

    Node tankNode;
    Shield shield;
    boolean force = false;
    boolean second = false;
    Vector3f walkDirection = new Vector3f(0, 0, 0);
    Vector3f viewDirection = new Vector3f(0, 0, 0);
    boolean forward = false, backward = false, leftRotate = false, rightRotate = false;
    private float airTime = 0;
    CharacterControl tankControl;
    Node bulletStartNode;
    Node walkDirNode;
    Main main;
    Dust dust;
    List<Bullet> bulletList;

    public Tank(Main main) {
        this.main = main;
        initTank();
    }

    private void initTank() {
        bulletList = new ArrayList<Bullet>();
        SphereCollisionShape sphere = new SphereCollisionShape(5f);
        tankControl = new CharacterControl(sphere, 0.01f);
        tankControl.setFallSpeed(15f);
        tankControl.setGravity(30f);
        tankNode = (Node) main.getAssetManager().loadModel("Models/HoverTank/Tank2.mesh.xml");
        tankNode.addControl(tankControl);
        tankNode.addControl(new RigidBodyControl(0));
        tankNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        tankControl.warp(new Vector3f(0, 315f, 0));


        bulletStartNode = new Node();
        bulletStartNode.setLocalTranslation(0, 3, 6);
        tankNode.attachChild(bulletStartNode);

        shield = new Shield(main);
        dust = new Dust(main);
        dust.emit.setParticlesPerSec(0f);
        tankNode.attachChild(dust.emit);
    }

    public void onAction(String binding, boolean isPressed, float tpf) {
        if (binding.equals("Rotate Left")) {
            if (isPressed) {
                leftRotate = true;
            } else {
                Quaternion quan = new Quaternion().fromAngles(0, 0, 0);
                tankNode.getChild(0).setLocalRotation(quan);
                leftRotate = false;
            }
        } else if (binding.equals("Rotate Right")) {
            if (isPressed) {
                rightRotate = true;
            } else {
                Quaternion quan = new Quaternion().fromAngles(0, 0, 0);
                tankNode.getChild(0).setLocalRotation(quan);
                rightRotate = false;
            }
        } else if (binding.equals("Walk Forward")) {
            if (isPressed) {
                forward = true;
                dust.emit.setParticlesPerSec(20);
            } else {
                forward = false;
                dust.emit.setParticlesPerSec(0);
            }
        } else if (binding.equals("Walk Backward")) {
            if (isPressed) {
                backward = true;
            } else {
                backward = false;
            }
        } else if (binding.equals("Shot") && isPressed) {
            RigidBodyControl phyBullet = new RigidBodyControl(0.0f);
            Bullet bullet = new Bullet(main, bulletStartNode.getWorldTranslation(),
                    tankNode.getWorldTranslation());
            bulletList.add(bullet);

            main.getRootNode().attachChild(bullet.geom);
            bullet.geom.addControl(phyBullet);
            main.bulletAppState.getPhysicsSpace().add(phyBullet);

        } else if (binding.equals("Shield")) {
            if (isPressed) {
                if (!second) {
                    force = true;
                    second = true;
                } else {
                    force = false;
                    second = false;
                }
            }
        }
    }

    public void updateTank(float tpf) {
        for (Bullet bullet : bulletList) {
            bullet.update(tpf);
        }


        Vector3f camDir = main.getCamera().getDirection().mult(0.2f);
        Vector3f camLeft = main.getCamera().getLeft().mult(0.2f);
        //System.out.println(camLeft);
        Quaternion rotLeft = new Quaternion().fromAngles(0, 0, -FastMath.PI * tpf / 4);
        Quaternion rotRight = new Quaternion().fromAngles(0, 0, FastMath.PI * tpf / 4);
        Quaternion resetRot = new Quaternion().fromAngles(0, 0, 0);
        Quaternion limLeft = new Quaternion().fromAngles(0, 0, -FastMath.PI / 4);
        Quaternion limRight = new Quaternion().fromAngles(0, 0, FastMath.PI / 4);
        camDir.y = 0;
        camLeft.y = 0;
        viewDirection.set(camDir);
        walkDirection.set(0, 0, 0);
        if (!tankControl.onGround()) {
            airTime = airTime + tpf;
        } else {
            airTime = 0;
        }
        if (forward) {
            walkDirection.addLocal(camDir.mult(5f));
            if (leftRotate) {
                viewDirection.addLocal(camLeft.mult(0.0075f));
                if (tankNode.getChild(0).getLocalRotation().getZ() >= limLeft.getZ()) {
                    tankNode.getChild(0).rotate(rotLeft);
                }
            } else if (rightRotate) {
                viewDirection.addLocal(camLeft.mult(0.0075f).negate());
                if (tankNode.getChild(0).getLocalRotation().getZ() <= limRight.getZ()) {
                    tankNode.getChild(0).rotate(rotRight);
                }
            } else {
                if (tankNode.getChild(0).getLocalRotation() != resetRot) {
                }

            }
        } else if (backward) {
            walkDirection.addLocal(camDir.mult(5f).negate());
            if (leftRotate) {
                viewDirection.addLocal(camLeft.mult(0.0075f).negate());
                if (tankNode.getChild(0).getLocalRotation().getZ() >= limLeft.getZ()) {
                    tankNode.getChild(0).rotate(rotLeft);
                }
            } else if (rightRotate) {
                viewDirection.addLocal(camLeft.mult(0.0075f));
                if (tankNode.getChild(0).getLocalRotation().getZ() <= limRight.getZ()) {
                    tankNode.getChild(0).rotate(rotRight);
                }
            } else {
                if (tankNode.getChild(0).getLocalRotation() != resetRot) {
                }
            }

        }
        if (force) {
            tankNode.attachChild(shield.nodeshield);
        } else if (!force) {
            tankNode.detachChild(shield.nodeshield);
        }
        tankControl.setWalkDirection(walkDirection);
        tankControl.setViewDirection(viewDirection);
        //System.out.println(walkDirection + "    " + viewDirection);
        if (airTime > 5f) {
            tankControl.setWalkDirection(Vector3f.ZERO);
        }
//        if (walkDirection.length() == 0) {
//            if (!"stand".equals(animationChannel.getAnimationName())) {
//                animationChannel.setAnim("stand", 1f);
//            }
//        } else {
//            viewDirection.set(camDir);
//            if (airTime > .3f) {
//                if (!"stand".equals(animationChannel.getAnimationName())) {
//                    animationChannel.setAnim("stand");
//                }
//            } else if (!"Walk".equals(animationChannel.getAnimationName())) {
//                animationChannel.setAnim("Walk", 0.7f);
//            }
//        }
    }
}
