/*
 * Decompiled with CFR 0.152.
 */
package com.cyberstep.ring.util;

import com.cyberstep.ring.util.ResourceSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public abstract class BasicResourceSource
implements ResourceSource {
    protected ObjectInputStream getObjectInputStream(InputStream inputStream) throws IOException {
        // 웹 포트 i18n: 직렬화 리소스 안의 문자열(이름·대사)을 읽는 즉시 번역한다
        if (!amped.web.I18n.active()) return new ObjectInputStream(inputStream);
        return new ObjectInputStream(inputStream) {
            { enableResolveObject(true); }
            protected Object resolveObject(Object obj) {
                return obj instanceof String ? amped.web.I18n.translate((String) obj) : obj;
            }
        };
    }

    protected abstract InputStream getInputStream(String var1) throws IOException;

    public abstract String[] getList(String var1);

    public final Object getObject(String string) {
        try {
            InputStream inputStream = this.getInputStream(string);
            if (inputStream == null) {
                return null;
            }
            try {
                ObjectInputStream objectInputStream = this.getObjectInputStream(inputStream);
                Object object = objectInputStream.readObject();
                if (string.startsWith("/accs/")) System.err.println("[amped.web] loaded " + string + " -> " + (object == null ? "null" : object.getClass().getName()));
                objectInputStream.close();
                return object;
            }
            catch (ClassNotFoundException classNotFoundException) {
                System.err.println("[BasicResourceSource#getObject] " + classNotFoundException);
                return null;
            }
            catch (Exception exception) {
                exception.printStackTrace();
                return null;
            }
        }
        catch (IOException iOException) {
            System.err.println("[BasicResourceSource#getObject] " + iOException);
            return null;
        }
    }

    public final byte[] getData(String string) {
        try {
            int n;
            InputStream inputStream = this.getInputStream(string);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while ((n = inputStream.read()) != -1) {
                byteArrayOutputStream.write(n);
            }
            return byteArrayOutputStream.toByteArray();
        }
        catch (IOException iOException) {
            System.err.println("[BasicResourceSource#getData] " + iOException);
            return null;
        }
    }
}

