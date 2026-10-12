/*
 * 게임 오버라이드(원본은 CFR 디컴파일). 변경점: amped.debug 프로퍼티가 있으면 기술 판정을 로그한다 (?debug=1).
 */
package com.cyberstep.ring.data;

import com.cyberstep.geometry.Quaternion;
import com.cyberstep.onstage.gfx.Structure;
import com.cyberstep.ring.data.ObjectData;
import com.cyberstep.ring.util.ResourceManager;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class PlayerData
extends ObjectData
implements Serializable {
    public static final int serialVersionUID = 1;
    public static final int FAULT = -1;
    public static final int CANCEL = -2;
    static final boolean DEBUG = System.getProperty("amped.debug") != null;
    public Vector techs = new Vector(20);
    public Hashtable attacks = new Hashtable(20);

    public void set(PlayerData playerData) {
        super.set(playerData);
        this.techs = playerData.techs;
        this.attacks = playerData.attacks;
        if (this.attacks == null) {
            this.attacks = new Hashtable(20);
        }
    }

    public Structure getAnimation() {
        Object v;
        Structure structure = null;
        if (structure == null && (v = this.resources.get("animation")) != null) {
            if (v instanceof String) {
                structure = (Structure)ResourceManager.getObject((String)v);
            } else if (v instanceof Structure) {
                structure = (Structure)v;
            }
        }
        if (structure == null) {
            structure = (Structure)ResourceManager.getObject("/player/anime/default.srt");
        }
        return structure;
    }

    public int getTechnique(byte by, int n) {
        int n2 = this.techs.size();
        int n3 = 0;
        while (n3 < n2) {
            if (((Technique)this.techs.elementAt(n3)).match(by, n)) {
                if (DEBUG) System.err.println("[tech] button=" + by + " status=" + n + " -> " + ((Technique)this.techs.elementAt(n3)).name + " (" + ((Technique)this.techs.elementAt(n3)).script + ")");
                return n3;
            }
            ++n3;
        }
        if (DEBUG) System.err.println("[tech] button=" + by + " status=" + n + " -> none");
        return -1;
    }

    public int getTechnique(String string, byte by, int n) {
        int n2;
        int n3;
        block4: {
            n3 = this.techs.size();
            n2 = 0;
            while (n2 < n3) {
                if (((Technique)this.techs.elementAt(n2)).isCancel(string, by, n)) {
                    ++n2;
                    continue;
                }
                break block4;
            }
            return -2;
        }
        n2 = 0;
        while (n2 < n3) {
            if (((Technique)this.techs.elementAt(n2)).match(string, by, n)) {
                if (DEBUG) System.err.println("[tech] chain from " + string + " button=" + by + " t=" + n + " -> " + ((Technique)this.techs.elementAt(n2)).name);
                return n2;
            }
            ++n2;
        }
        return -1;
    }

    public Technique techAt(int n) {
        return (Technique)this.techs.elementAt(n);
    }

    public boolean addTechnique(String string, byte by, int n, String string2, String string3, int n2, int n3) {
        int n4 = this.techs.size();
        int n5 = 0;
        while (n5 < n4) {
            if (((Technique)this.techs.elementAt((int)n5)).name.equals(string)) {
                return false;
            }
            ++n5;
        }
        Technique technique = new Technique();
        technique.name = string;
        technique.button = by;
        technique.status = n;
        technique.script = string2;
        technique.prevtech = string3;
        technique.from = n2;
        technique.to = n3;
        this.techs.add(technique);
        return true;
    }

    public boolean addTechnique(String string) {
        return this.addTechnique(string, (byte)1, 0, "", "", 0, 0);
    }

    public void deleteTechnique(String string) {
        int n = this.techs.size();
        int n2 = 0;
        while (n2 < n) {
            if (((Technique)this.techs.elementAt((int)n2)).name.equals(string)) {
                this.techs.remove(n2);
                break;
            }
            ++n2;
        }
    }

    public Technique getTechnique(String string) {
        int n = this.techs.size();
        int n2 = 0;
        while (n2 < n) {
            Technique technique = (Technique)this.techs.elementAt(n2);
            if (technique.name.equals(string)) {
                return technique;
            }
            ++n2;
        }
        return null;
    }

    public int getTechIndex(String string) {
        int n = this.techs.size();
        if (string == null) {
            return -1;
        }
        int n2 = 0;
        while (n2 < n) {
            Technique technique = (Technique)this.techs.elementAt(n2);
            if (technique.name.equals(string)) {
                return n2;
            }
            ++n2;
        }
        return -1;
    }

    public void renameTechnique(String string, String string2) {
        int n = this.techs.size();
        int n2 = 0;
        while (n2 < n) {
            Technique technique = (Technique)this.techs.elementAt(n2);
            if (technique.name.equals(string)) {
                technique.name = string2;
            }
            ++n2;
        }
    }

    public String[] getTechniqueNames() {
        int n = this.techs.size();
        String[] stringArray = new String[n];
        int n2 = 0;
        while (n2 < n) {
            stringArray[n2] = ((Technique)this.techs.elementAt((int)n2)).name;
            ++n2;
        }
        return stringArray;
    }

    public Attack newAttack(String string) {
        Attack attack = (Attack)this.attacks.get(string);
        if (attack == null) {
            return new Attack();
        }
        return null;
    }

    public Attack getAttack(String string) {
        return (Attack)this.attacks.get(string);
    }

    public String toString() {
        String string = "PlayerData details...\n";
        Object object = this.resources.get("animation");
        if (object == null) {
            string = string + "Animation : default.\n";
        } else if (object instanceof String) {
            string = string + "Animation : reference-" + object + ".\n";
        } else if (object == null) {
            string = string + "Animation : own.\n";
        }
        string = string + "Techniques of " + this.techs.size() + "...\n";
        int n = 0;
        while (n < this.techs.size()) {
            string = string + "\t" + ((Technique)this.techs.elementAt(n)).toString() + "\n";
            ++n;
        }
        string = string + "Attacks of " + this.attacks.size() + "...\n";
        Enumeration enumeration = this.attacks.keys();
        while (enumeration.hasMoreElements()) {
            object = enumeration.nextElement();
            string = string + "\t" + object + ":" + this.attacks.get(object) + "\n";
        }
        return string;
    }

    public static class Attack
    implements Serializable {
        public static final int serialVersionUID = 1;
        public String attackmodel = "";
        public String effect;
        public float power = 0.0f;
        public float str;
        public int time = 0;
        public float x = 0.0f;
        public float y = 0.0f;
        public float z = 0.0f;
        public float pitch = 0.0f;
        public String feedback = "";

        public String toString() {
            String string = "pow:" + this.power + "(" + this.str + ")" + " time:" + this.time + " pos:" + new Quaternion(this.pitch, this.x, this.y, this.z);
            if (this.feedback != null && !this.feedback.equals("")) {
                string = string + " feedback:" + this.feedback;
            }
            return string;
        }
    }

    public static class Technique
    implements Serializable,
    Comparable {
        public static final int serialVersionUID = 1;
        public String name;
        public byte button;
        public int status;
        public String script;
        public String prevtech;
        public int from;
        public int to;

        public int compareTo(Object object) {
            if (object instanceof Technique) {
                return this.name.compareTo(((Technique)object).name);
            }
            return this.name.compareTo(String.valueOf(object)); // CFR 디컴파일 보정
        }

        public boolean match(byte by, int n) {
            return this.button == by && (this.status & n) != 0;
        }

        public boolean isCancel(String string, byte by, int n) {
            return this.prevtech == null || !this.prevtech.equals(string) || this.button != by;
        }

        public boolean match(String string, byte by, int n) {
            return this.prevtech != null && this.prevtech.equals(string) && this.button == by && n >= this.from && n <= this.to;
        }

        public String toString() {
            String string = this.name + " ";
            switch (this.button) {
                case 1: {
                    string = string + "A ";
                    break;
                }
                case 2: {
                    string = string + "B ";
                    break;
                }
                case 3: {
                    string = string + "AB";
                }
            }
            string = string + " " + this.script;
            string = string + " stat:" + this.statusString();
            if (this.prevtech != null && !this.prevtech.equals("")) {
                string = string + " prev:" + this.prevtech + "[" + this.from + "-" + this.to + "]";
            }
            return string;
        }

        public String statusString() {
            String string = "";
            if ((this.status & 1) != 0) {
                string = string + "S";
            }
            if ((this.status & 2) != 0) {
                string = string + "W";
            }
            if ((this.status & 4) != 0) {
                string = string + "R";
            }
            if ((this.status & 8) != 0) {
                string = string + "B";
            }
            if ((this.status & 0x10) != 0) {
                string = string + "*";
            }
            if ((this.status & 0x20) != 0) {
                string = string + "F";
            }
            if ((this.status & 0x40) != 0) {
                string = string + "*";
            }
            if ((this.status & 0x80) != 0) {
                string = string + "*";
            }
            if ((this.status & 0x800) != 0) {
                string = string + "A";
            }
            return string;
        }
    }
}

