package backstage.util;

import java.io.Serializable;
import java.security.*;
import java.util.Iterator;
import keel.resource.Archive;
import keel.resource.ArchiveClassLoader;
import keel.resource.Resource;

/** 웹 포트: .clazz 는 이미 복호화되어 있으므로 defineClass 를 직접 호출한다(keel_clazz.dll 대체). */
public class CodedClassLoader extends ArchiveClassLoader {
    private String suffix;
    private ProtectionDomain defaultDomain = null;

    public CodedClassLoader() { this(ClassLoader.getSystemClassLoader()); }
    public CodedClassLoader(ClassLoader parent) { this(parent, ".clazz"); }
    public CodedClassLoader(ClassLoader parent, String suffix) { super(parent); this.suffix = suffix; }

    protected synchronized Class findClass(final String name) throws ClassNotFoundException {
        String path = '/' + name.replace('.', '/').concat(suffix);
        Resource res = null;
        Iterator it = getAllArchives().iterator();
        while (it.hasNext()) {
            Archive archive = (Archive) it.next();
            res = archive.getResource(path);
            if (res != null) break;
        }
        if (res == null) return super.findClass(name);
        Serializable buff = res.getContentWithoutCache();
        if (!(buff instanceof byte[])) throw new ClassNotFoundException(name);
        byte[] b = amped.web.I18n.patchClass((byte[]) buff);
        int index = name.lastIndexOf('.');
        if (index >= 0) {
            String pkgName = name.substring(0, index);
            if (getPackage(pkgName) == null) {
                try { definePackage(pkgName, null, null, null, null, null, null, null); } catch (IllegalArgumentException e) { }
            }
        }
        return defineClass(name, b, 0, b.length, getDefaultDomain());
    }

    private ProtectionDomain getDefaultDomain() {
        if (defaultDomain == null) {
            CodeSource cs = new CodeSource(null, (java.security.cert.Certificate[]) null);
            PermissionCollection pc = new Permissions();
            pc.add(new AllPermission());
            defaultDomain = new ProtectionDomain(cs, pc);
        }
        return defaultDomain;
    }
}
