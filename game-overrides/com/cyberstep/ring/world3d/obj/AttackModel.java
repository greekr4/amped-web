/*
 * 게임 클래스 오버라이드 (CFR 디컴파일 → 수정). 원본과의 차이:
 *   - 유도 발사체: AttackData resources 에 "HOMING"(Float, 초당 이동량) 이 있으면 매 프레임
 *     가장 가까운 살아 있는 적 플레이어 쪽으로 궤적을 휘어 간다(원본 엔진엔 추적이 없고 correctvec 도 안 쓰인다).
 *     poa 스펙의 `aoa <키> <바탕> homing=<속도>` 로 켠다. 디버그(amped.debug)면 [homing] 로그.
 *   - CFR 산출물의 미초기화 변수(f6/f7/f8) 를 f 로 고침.
 */
package com.cyberstep.ring.world3d.obj;

import com.cyberstep.geometry.Matrix;
import com.cyberstep.geometry.Quaternion;
import com.cyberstep.geometry.Vector3D;
import com.cyberstep.onstage.gfx.GFXEnum;
import com.cyberstep.onstage.util.Sys;
import com.cyberstep.ring.data.AttackData;
import com.cyberstep.ring.data.ObjectData;
import com.cyberstep.ring.util.ScriptEngineVariable;
import com.cyberstep.ring.world3d.BasicModel;
import com.cyberstep.ring.world3d.WorldModel3D;
import com.cyberstep.ring.world3d.check.Bound3D;
import com.cyberstep.ring.world3d.check.Rect3D;
import com.cyberstep.ring.world3d.obj.Body3D;
import java.util.Vector;

public class AttackModel
extends Body3D
implements GFXEnum,
ScriptEngineVariable {
    public static final int BEFORE_START = 0;
    public static final int RUNNING = 1;
    public static final int END = 2;
    int power;
    String effect;
    Vector motion;
    boolean iscurve;
    boolean hasparent;
    boolean ignoreguard;
    byte effecttype;
    private int status;
    long starttime;
    float totaltime;
    private float prevdt;
    float reflectance;
    float lastspd = 0.0f;
    Vector3D lastvec = new Vector3D();
    Body3D parent;
    final Matrix basem = new Matrix();
    AttackData source;
    protected float scale = 1.0f;
    boolean rotfree;
    // ---- 유도 (amped-web 추가) ----
    static final boolean DEBUG = System.getProperty("amped.debug") != null;
    float homingSpeed = 0.0f;           // 초당 유도 이동량(0 이면 유도 없음)
    final Vector3D homingOff = new Vector3D(); // 원래 궤적에 더해지는 누적 보정량
    Body3D homingTarget;

    public AttackModel(WorldModel3D worldModel3D) {
        super(worldModel3D);
        this.makeScriptEngine(this);
    }

    public AttackModel(Body3D body3D, int n, AttackData attackData, Vector3D vector3D, Vector3D vector3D2, String string, float f) {
        super(body3D.world);
        this.doesNotify = false;
        this.setAttack(body3D, n, attackData, vector3D, vector3D2, string, f);
        this.makeScriptEngine(this);
    }

    protected void setAttack(Body3D body3D, int n, AttackData attackData, Vector3D vector3D, Vector3D vector3D2, String string, float f) {
        if (body3D == null) {
            System.err.println("PARENT OBJECT not found at obj.AttackModel.setAttack()");
        }
        body3D.updateMatrix();
        this.setObjectData(attackData);
        this.parent = this.attackorg = body3D;
        Vector3D vector3D3 = Vector3D.velocity(vector3D, vector3D2);
        Quaternion quaternion = Quaternion.getAimQuaternion(vector3D3.unit(), Vector3D.zaxis);
        if (!attackData.correctvert) {
            vector3D3.y = 0.0f;
            this.rotation.set(Quaternion.getAimQuaternion(vector3D3.unit(), Vector3D.zaxis));
            quaternion.set(this.rotation);
        } else if (this.rotfree) {
            this.rotation.set(quaternion);
        } else {
            vector3D3.y = 0.0f;
            this.rotation.set(Quaternion.getAimQuaternion(vector3D3.unit(), Vector3D.zaxis));
        }
        if (attackData.getResource("RESCALE") != null) {
            float[] m = body3D.getMatrix().matrix;
            float f2 = m[0] * m[0] + m[4] * m[4] + m[8] * m[8];
            float f3 = m[1] * m[1] + m[5] * m[5] + m[9] * m[9];
            float f4 = m[2] * m[2] + m[6] * m[6] + m[10] * m[10];
            this.scale = (float)Math.sqrt(Math.min(f2, Math.min(f3, f4)));
        }
        this.basem.init().translate(vector3D).rotate(quaternion);
        if (this.hasparent) {
            Matrix copy = new Matrix(this.basem);
            this.basem.set(this.parent.getMatrix()).inverse().mult(copy);
        }
        this.location.set(body3D.location);
        this.power = n;
        this.totaltime = f;
        this.effect = string;
        this.updateMatrix();
        this.status = 0;
    }

    public void reset() {
        this.status = 0;
    }

    public void start() {
        this.start(Sys.getTime());
    }

    public void start(long l) {
        if (this.status == 0) {
            this.status = 1;
            this.starttime = l;
            this.prevdt = 0.0f;
            if (this.setScript("attack.sc")) {
                this.startScript(l);
            }
        }
    }

    public void setObjectData(ObjectData objectData) {
        super.setObjectData(objectData);
        if (objectData instanceof AttackData) {
            AttackData attackData;
            this.source = attackData = (AttackData)objectData;
            this.motion = attackData.motion;
            this.iscurve = attackData.iscurve;
            this.hasparent = attackData.hasparent;
            this.effecttype = (byte)attackData.effecttype;
            this.ignoreguard = attackData.ignoreguard;
            this.rotfree = this.getResource("ROT_FREE") != null;
            Object h = attackData.resources == null ? null : attackData.resources.get("HOMING");
            this.homingSpeed = h instanceof Number ? ((Number)h).floatValue() : 0.0f;
        }
    }

    public boolean spendTime(float f) {
        this.executeScript();
        if (!this.avilable) {
            return false;
        }
        if (this.status != 1) {
            return this.avilable;
        }
        return this.attackMove();
    }

    public boolean attackMove() {
        Vector3D vector3D;
        float f;
        Vector3D vector3D2 = new Vector3D();
        Vector3D vector3D3 = new Vector3D();
        long l = Sys.getTime();
        float f2 = (float)(l - this.starttime) / this.totaltime;
        f2 = Math.max(f2, 0.0f);
        f2 = Math.min(f2, 1.0f);
        Vector3D vector3D4 = new Vector3D(this.location);
        this.setFrame(f2);
        if (this.homingSpeed > 0.0f) {
            this.homing((f2 - this.prevdt) * this.totaltime * 0.001f);
        }
        this.location.add(this.homingOff);
        Vector3D vector3D5 = Vector3D.velocity(vector3D4, this.location);
        Vector3D vector3D6 = new Vector3D();
        if (this.prevdt == 0.0f) {
            this.setFrame(0.2f);
            vector3D2.set(this.location);
            this.setFrame(0.0f);
            vector3D6.set(vector3D2).sub(this.location);
            vector3D6.set(vector3D6.x, 0.0f, vector3D6.z).unit();
            this.setFrame(f2);
            this.location.add(this.homingOff);
        } else {
            vector3D6.set(vector3D5.x, 0.0f, vector3D5.z).unit();
        }
        this.location.sub(vector3D5);
        float f3 = (f2 - this.prevdt) * this.totaltime * 0.001f;
        if (this.prevdt == 0.0f) {
            f3 = 0.0f;
        }
        float f4 = vector3D5.length();
        Rect3D rect3D = this.getEnvironRect(vector3D5);
        int n = this.world.getNearObjects(rect3D, this.nearobj);
        int n2 = this.checknum > 0 ? this.world.getNearMapParts(rect3D, this.nearmap) : 0;
        float f5 = 100.0f;
        int n3 = 0;
        while (n3 < n2) {
            f = this.nearmap[n3].check(this.location, vector3D5, vector3D2);
            if (f > 0.0f && f < f5) {
                f5 = f;
                vector3D3.set(this.nearmap[n3].v);
            }
            ++n3;
        }
        n3 = 0;
        while (n3 < n) {
            if (this.nearobj[n3] != this && this.nearobj[n3] != this.parent) {
                f = this.nearobj[n3].check(this, vector3D5, vector3D2, 1);
                if (f > 0.0f && f < f5) {
                    f5 = f;
                    vector3D3.set(vector3D2).unit();
                }
            }
            ++n3;
        }
        n3 = 0;
        while (n3 < n) {
            if (this.nearobj[n3] != this && this.nearobj[n3] != this.parent) {
                f = this.nearobj[n3].check(this, vector3D5, vector3D2, 2);
                if (f > 0.0f && f <= f5) {
                    vector3D = new Vector3D(vector3D5).scale(f).add(this.location);
                    this.crash(vector3D, vector3D6, this.nearobj[n3]);
                }
            }
            ++n3;
        }
        if (f5 < 1.0f) {
            vector3D5.scale(f5);
        }
        this.location.add(vector3D5);
        if (f5 != 100.0f) {
            return this.end(vector3D5, vector3D3, f3);
        }
        this.lastvec.set(vector3D5);
        this.updateMatrix();
        this.updateBounds();
        this.updateChecks();
        n3 = 0;
        while (n3 < n2) {
            int n4 = 0;
            while (n4 < this.checknum) {
                if (this.pcm[n4].check(this.nearmap[n3], this.location, vector3D2) > 0.0f) {
                    this.location.add(vector3D2);
                    return this.end(vector3D5, this.nearmap[n3].v, f3);
                }
                ++n4;
            }
            if (this.foot.check(this.nearmap[n3], this.location, vector3D2) > 0.0f) {
                this.location.add(vector3D2);
                return this.end(vector3D5, this.nearmap[n3].v, f3);
            }
            ++n3;
        }
        n3 = 0;
        while (n3 < n) {
            if (this.nearobj[n3] != this && this.nearobj[n3] != this.parent) {
                if (this.nearobj[n3].check(this, vector3D2, 2) > 0.0f) {
                    vector3D = vector3D2.add(this.location);
                    if (vector3D6.x == 0.0f && vector3D6.z == 0.0f) {
                        vector3D6.set(this.nearobj[n3].location).sub(this.location);
                        vector3D6.y = 0.0f;
                        vector3D6.unit();
                        this.crash(vector3D, vector3D6, this.nearobj[n3]);
                        vector3D6.set(0.0f, 0.0f, 0.0f);
                    } else {
                        this.crash(vector3D, vector3D6, this.nearobj[n3]);
                    }
                }
                if (this.nearobj[n3].check(this, vector3D2, 1) > 0.0f) {
                    return this.end(vector3D5, vector3D2, f3);
                }
            }
            ++n3;
        }
        if (f2 == 1.0f) {
            if (f3 > 0.0f) {
                return this.end(vector3D5.scale(1.0f / f3));
            }
            return this.end(vector3D5.scale(0.0f));
        }
        this.prevdt = f2;
        this.lastspd = f3;
        return true;
    }

    /** 가장 가까운 살아 있는 적 플레이어를 향해 보정량(homingOff)을 키운다. dtSec 는 이번 프레임 경과(초). */
    void homing(float dtSec) {
        if (dtSec <= 0.0f || this.world == null) return;
        if (this.homingTarget == null || (this.homingTarget instanceof Player && ((Player)this.homingTarget).isDead())) {
            this.homingTarget = null;
            // getPlayerModels() 는 사람 플레이어만 돌려준다(적 NPC 제외). NPC 는 Player 의 하위 클래스라 전체 모델에서 Player 를 고른다.
            Body3D[] all = this.world.getAllModels();
            int cnt = 0; if (all != null) for (int i = 0; i < all.length; i++) if (all[i] instanceof Player) cnt++;
            Body3D[] players = new Body3D[cnt]; cnt = 0;
            if (all != null) for (int i = 0; i < all.length; i++) if (all[i] instanceof Player) players[cnt++] = all[i];
            float best = 1.0E9f;
            if (players != null) {
                for (int i = 0; i < players.length; i++) {
                    Body3D b = players[i];
                    if (b == null || b == this.parent || b == this.attackorg || b.HP <= 0) continue;
                    if (b instanceof Player && ((Player)b).isDead()) continue;
                    float d = Vector3D.velocity(this.location, b.location).length();
                    if (d < best) { best = d; this.homingTarget = b; }
                }
            }
            if (DEBUG && this.homingTarget != null) System.err.println("[homing] target=" + this.homingTarget.getName() + " dist=" + best + " candidates=" + players.length);
        }
        if (this.homingTarget == null) return;
        Vector3D want = new Vector3D(this.homingTarget.location);
        want.y += 0.3f; // 몸통 높이
        Vector3D cur = new Vector3D(this.location).add(this.homingOff);
        Vector3D d = Vector3D.velocity(cur, want);
        float len = d.length();
        float step = this.homingSpeed * dtSec;
        if (len <= step) { this.homingOff.add(d); return; }
        d.scale(step / len);
        this.homingOff.add(d);
    }

    protected final Vector3D reflect(Vector3D vector3D, Vector3D vector3D2) {
        Vector3D vector3D3 = new Vector3D(vector3D);
        float f = vector3D.length();
        Vector3D vector3D4 = vector3D2.unit(new Vector3D());
        float f2 = Vector3D.dot(vector3D3.unit(new Vector3D()), vector3D4) * this.reflectance * -f;
        vector3D4.scale(f2);
        vector3D3.add(vector3D4);
        if (vector3D3.length() < 0.1f) {
            vector3D3.set(0.0f, 0.0f, 0.0f);
        }
        return vector3D3;
    }

    public void crash(Vector3D vector3D, Vector3D vector3D2, Body3D body3D) {
        int n = body3D.HP;
        if (body3D.attack(this.parent, this.power, this.effect, vector3D2, this.ignoreguard)) {
            this.notifyMakeEffect(this.effecttype, vector3D, vector3D2);
            this.parent.hitAttack(vector3D, vector3D2, body3D, n -= body3D.HP);
        }
    }

    public boolean end(Vector3D vector3D) {
        this.status = 2;
        this.rotation.set(BasicModel.aimQuaternion(vector3D));
        this.updateMatrix();
        this.parent = null;
        if (this.setScript("end.sc")) {
            this.startScript(Sys.getTime());
            return true;
        }
        return false;
    }

    public boolean end(Vector3D vector3D, Vector3D vector3D2, float f) {
        if (f > 0.0f) {
            Vector3D vector3D3 = this.reflect(vector3D.scale(1.0f / f), vector3D2);
        } else {
            Vector3D vector3D4 = new Vector3D();
        }
        if (this.parent != null) {
            this.parent.hitAttack(this.location, vector3D, null, 0);
        }
        this.status = 2;
        this.rotation.set(BasicModel.aimQuaternion(vector3D2.unit()));
        this.updateMatrix();
        this.parent = null;
        if (this.setScript("end.sc")) {
            this.startScript(Sys.getTime());
            return true;
        }
        return false;
    }

    public final void setFrame(float f) {
        Matrix matrix = new Matrix();
        if (this.parent != null && this.hasparent) {
            matrix.set(this.parent.getMatrix()).mult(this.basem);
        } else {
            matrix.set(this.basem);
        }
        if (this.source != null) {
            Bound3D bound3D = this.source.getMotion(f);
            this.location.set(bound3D.x, bound3D.y, bound3D.z);
            float f2 = bound3D.size * this.scale;
            this.size.set(f2, f2, f2);
            this.location.transform(matrix);
        }
    }

    public final void setFrame(float f, Vector3D vector3D, Vector3D vector3D2) {
        Matrix matrix = new Matrix();
        if (this.parent != null && this.hasparent) {
            matrix.set(this.parent.getMatrix()).mult(this.basem);
        } else {
            matrix.set(this.basem);
        }
        if (this.source != null) {
            Bound3D bound3D = this.source.getMotion(f);
            vector3D.set(bound3D.x, bound3D.y, bound3D.z);
            float f2 = bound3D.size * this.scale;
            vector3D.set(f2, f2, f2);
            this.location.transform(matrix);
        }
    }
}
