/*
 * Decompiled with CFR 0.152.
 */
package com.cyberstep.ring.util;

import com.cyberstep.ring.util.BasicResourceSource;
import com.cyberstep.ring.util.FileUtil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Vector;

public class FileResourceSource
extends BasicResourceSource {
    String resourceDir;

    public FileResourceSource() {
        this("/resource/");
    }

    public FileResourceSource(String string) {
        this.resourceDir = System.getProperty("user.dir") + string;
    }

    protected InputStream getInputStream(String string) throws IOException {
        // 웹 포트 i18n: 언어별 덮어쓰기 파일(/app/game/l10n/<lang>/resource/...)이 있으면 그것을 쓴다
        File ov = amped.web.I18n.override(string);
        if (ov != null) return new BufferedInputStream(new FileInputStream(ov), 4096);
        return new BufferedInputStream(new FileInputStream(this.resourceDir + string), 4096);
    }

    public String[] getList(String string) {
        String[] stringArray = null;
        stringArray = FileUtil.getFileNameList(this.resourceDir + string);
        Vector<String> vector = new Vector<String>(stringArray.length);
        int n = 0;
        while (n < stringArray.length) {
            vector.addElement(stringArray[n]);
            ++n;
        }
        Collections.sort(vector);
        return vector.toArray(stringArray);
    }
}

