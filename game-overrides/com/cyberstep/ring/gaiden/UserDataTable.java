/*
 * 게임 오버라이드(원본은 CFR 디컴파일). 시스템 프로퍼티 amped.accs 가 있으면
 * 싱글 플레이(외전) 플레이어 캐릭터 5명에게 /accs/<amped.accs>.accs 를 장착한다. (?accs=heavygloves)
 */
package com.cyberstep.ring.gaiden;

import com.cyberstep.ring.data.NPCData;
import com.cyberstep.ring.data.UserData;
import com.cyberstep.ring.data.UserDataEx;
import com.cyberstep.ring.util.Dict;
import com.cyberstep.ring.util.ResourceManager;
import java.util.Hashtable;

public class UserDataTable
extends Hashtable {
    public static final UserDataTable players = new UserDataTable();

    public UserDataTable() {
        UserData userData = new UserData();
        userData.name = Dict.get("\u30b8\u30e3\u30c3\u30ad\u30fc\u306e\u307c\u308b");
        userData.style = "KAKUTOU_M";
        userData.head = "sun1.prm";
        userData.face = "01.face";
        userData.body = "1_k_4_1.body";
        UserData userData2 = new UserData();
        userData2.name = Dict.get("\u30de\u30a4\u30af\u30fb\u30c7\u30a4\u30d3\u30b9");
        userData2.style = "ARMY_W";
        userData2.head = "met1.prm";
        userData2.face = "04.face";
        userData2.body = "2_a_1_w_1.body";
        userData2.equipment = "beamrifle";
        UserData userData3 = new UserData();
        userData3.name = Dict.get("\u30ea\u30f3\u30c0\u30fb\u30dc\u30a6\u30dc\u30a6");
        userData3.style = "SPYB";
        userData3.head = "afuro1.prm";
        userData3.face = "bo1.face";
        userData3.body = "4_1_b_1.body";
        userData3.equipment = "pison";
        UserData userData4 = new UserData();
        userData4.name = Dict.get("\u30d5\u30a1\u30d3\u30af\u30ed");
        userData4.style = "SUPERMAN_Y";
        userData4.head = "aasuper3.prm";
        userData4.face = "04.face";
        userData4.body = "6_2_1.body";
        userData4.equipment = "beamsaber";
        UserData userData5 = new UserData();
        userData5.name = Dict.get("\u30d0\u30d5\u30a1\u30dc\u30f3");
        userData5.style = "ARMOR_W";
        userData5.head = "tun1.prm";
        userData5.face = "16.face";
        userData5.body = "5.body";
        userData5.equipment = "harisen";
        UserDataEx userDataEx = new UserDataEx((NPCData)ResourceManager.getObject("/component/gaiden/npc/core.npc"));
        UserDataEx userDataEx2 = new UserDataEx((NPCData)ResourceManager.getObject("/component/gaiden/npc/taisa.npc"));
        UserDataEx userDataEx3 = new UserDataEx((NPCData)ResourceManager.getObject("/component/gaiden/npc/bon.npc"));
        UserDataEx userDataEx4 = new UserDataEx((NPCData)ResourceManager.getObject("/component/gaiden/npc/bob.npc"));
        UserDataEx userDataEx5 = new UserDataEx((NPCData)ResourceManager.getObject("/component/gaiden/npc/bobooi.npc"));
        UserDataEx userDataEx6 = new UserDataEx((NPCData)ResourceManager.getObject("/component/gaiden/npc/greater.npc"));
        UserDataEx userDataEx7 = new UserDataEx((NPCData)ResourceManager.getObject("/component/gaiden/npc/fabicro3.npc"));
        UserDataEx userDataEx8 = new UserDataEx((NPCData)ResourceManager.getObject("/component/gaiden/npc/gori.npc"));
        UserDataEx userDataEx9 = new UserDataEx((NPCData)ResourceManager.getObject("/component/gaiden/npc/dragon_p.npc"));
        String accs = System.getProperty("amped.accs");
        if (accs != null && !accs.equals("")) {
            UserData[] pcs = {userData, userData2, userData3, userData4, userData5};
            for (int i = 0; i < pcs.length; ++i) pcs[i].setAccessoryData("/accs/" + accs + ".accs");
            System.err.println("[amped.web] accessory: /accs/" + accs + ".accs");
        }
        this.put("noboru", userData);
        this.put("mike", userData2);
        this.put("linda", userData3);
        this.put("fabicro", userData4);
        this.put("bafabon", userData5);
        this.put("core", userDataEx);
        this.put("taisa", userDataEx2);
        this.put("bon", userDataEx3);
        this.put("bob", userDataEx4);
        this.put("bobooi", userDataEx5);
        this.put("greater", userDataEx6);
        this.put("fabicro3", userDataEx7);
        this.put("gori", userDataEx8);
        this.put("dragon", userDataEx9);
    }

    public static UserData getUserData(String string) {
        return (UserData)players.get(string);
    }

    public static void putUserData(String string, UserData userData) {
        players.put(string, userData);
    }
}

