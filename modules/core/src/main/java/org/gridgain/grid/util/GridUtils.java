/* 
 Copyright (C) GridGain Systems. All Rights Reserved.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.util;

import org.gridgain.grid.*;
import org.gridgain.grid.cache.*;
import org.gridgain.grid.compute.*;
import org.gridgain.grid.events.*;
import org.gridgain.grid.kernal.*;
import org.gridgain.grid.kernal.managers.deployment.*;
import org.gridgain.grid.kernal.processors.cache.*;
import org.gridgain.grid.kernal.processors.streamer.*;
import org.gridgain.grid.lang.*;
import org.gridgain.grid.logger.*;
import org.gridgain.grid.portables.*;
import org.gridgain.grid.product.*;
import org.gridgain.grid.spi.*;
import org.gridgain.grid.spi.discovery.*;
import org.gridgain.grid.util.io.*;
import org.gridgain.grid.util.lang.*;
import org.gridgain.grid.util.mbean.*;
import org.gridgain.grid.util.typedef.*;
import org.gridgain.grid.util.typedef.internal.*;
import org.gridgain.grid.util.worker.*;
import org.jdk8.backport.*;
import org.jetbrains.annotations.*;
import sun.misc.*;

import javax.management.*;
import javax.naming.*;
import javax.net.ssl.*;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.management.*;
import java.lang.reflect.Array;
import java.lang.reflect.*;
import java.math.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.channels.spi.*;
import java.nio.charset.*;
import java.security.*;
import java.security.cert.*;
import java.sql.*;
import java.sql.Timestamp;
import java.text.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.jar.*;
import java.util.logging.*;
import java.util.regex.*;
import java.util.zip.*;

import static org.gridgain.grid.GridSystemProperties.*;
import static org.gridgain.grid.events.GridEventType.*;
import static org.gridgain.grid.kernal.GridNodeAttributes.*;

/**
 * Collection of utility methods used throughout the system.
 */
@SuppressWarnings({"UnusedReturnValue", "UnnecessaryFullyQualifiedName"})
public abstract class GridUtils {
    /** Unsafe. */
    private static final Unsafe UNSAFE = GridUnsafe.unsafe();

    /** {@code True} if {@code unsafe} should be used for array copy. */
    private static final boolean UNSAFE_BYTE_ARR_CP = unsafeByteArrayCopyAvailable();

    /** Offset. */
    private static final int BYTE_ARRAY_DATA_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);

    /** Sun-specific JDK constructor factory for objects that don't have empty constructor. */
    private static final Method CTOR_FACTORY;

    /** Sun JDK reflection factory. */
    private static final Object SUN_REFLECT_FACTORY;

    /** Public {@code java.lang.Object} no-argument constructor. */
    private static final Constructor OBJECT_CTOR;

    /** All grid event names. */
    private static final Map<Integer, String> GRID_EVT_NAMES = new HashMap<>();

    /** All grid events. */
    private static final int[] GRID_EVTS;

    /** Empty integers array. */
    private static final int[] EMPTY_INTS = new int[0];

    /** Empty  longs. */
    private static final long[] EMPTY_LONGS = new long[0];

    /** System line separator. */
    private static final String NL = System.getProperty("line.separator");

    /** Default user version. */
    public static final String DFLT_USER_VERSION = "0";

    /** Cache for {@link GridPeerDeployAware} fields to speed up reflection. */
    private static final ConcurrentMap<String, GridBiTuple<Class<?>, Collection<Field>>> p2pFields =
        new ConcurrentHashMap8<>();

    /** Secure socket protocol to use. */
    private static final String HTTPS_PROTOCOL = "TLS";

    /** Project home directory. */
    private static volatile GridTuple<String> ggHome;

    /** Project work directory. */
    private static volatile String ggWork;

    /** OS JDK string. */
    private static String osJdkStr;

    /** OS string. */
    private static String osStr;

    /** JDK string. */
    private static String jdkStr;

    /** Indicates whether current OS is Windows 95. */
    private static boolean win95;

    /** Indicates whether current OS is Windows 98. */
    private static boolean win98;

    /** Indicates whether current OS is Windows NT. */
    private static boolean winNt;

    /** Indicates whether current OS is Windows Vista. */
    private static boolean winVista;

    /** Indicates whether current OS is Windows 7. */
    private static boolean win7;

    /** Indicates whether current OS is Windows 8. */
    private static boolean win8;

    /** Indicates whether current OS is Windows 8.1. */
    private static boolean win81;

    /** Indicates whether current OS is some version of Windows. */
    private static boolean unknownWin;

    /** Indicates whether current OS is Windows 2000. */
    private static boolean win2k;

    /** Indicates whether current OS is Windows XP. */
    private static boolean winXp;

    /** Indicates whether current OS is Windows Server 2003. */
    private static boolean win2003;

    /** Indicates whether current OS is Windows Server 2008. */
    private static boolean win2008;

    /** Indicates whether current OS is UNIX flavor. */
    private static boolean unix;

    /** Indicates whether current OS is Solaris. */
    private static boolean solaris;

    /** Indicates whether current OS is Linux flavor. */
    private static boolean linux;

    /** Indicates whether current OS is NetWare. */
    private static boolean netware;

    /** Indicates whether current OS is Mac OS. */
    private static boolean mac;

    /** Indicates whether current OS architecture is Sun Sparc. */
    private static boolean sparc;

    /** Indicates whether current OS architecture is Intel X86. */
    private static boolean x86;

    /** Name of the underlying OS. */
    private static String osName;

    /** Version of the underlying OS. */
    private static String osVer;

    /** CPU architecture of the underlying OS. */
    private static String osArch;

    /** Name of the Java Runtime. */
    private static String javaRtName;

    /** Name of the Java Runtime version. */
    private static String javaRtVer;

    /** Name of the JDK vendor. */
    private static String jdkVendor;

    /** Name of the JDK. */
    private static String jdkName;

    /** Version of the JDK. */
    private static String jdkVer;

    /** Name of JVM specification. */
    private static String jvmSpecName;

    /** Version of JVM implementation. */
    private static String jvmImplVer;

    /** Vendor's name of JVM implementation. */
    private static String jvmImplVendor;

    /** Name of the JVM implementation. */
    private static String jvmImplName;

    /** JMX domain as 'xxx.gridgain'. */
    public static final String JMX_DOMAIN = GridUtils.class.getName().substring(0, GridUtils.class.getName().
        indexOf('.', GridUtils.class.getName().indexOf('.') + 1));

    /** Network packet header. */
    public static final byte[] GG_HEADER = U.intToBytes(0x00004747);

    /** Default buffer size = 4K. */
    private static final int BUF_SIZE = 4096;

    /** Byte bit-mask. */
    private static final int MASK = 0xf;

    /** Long date format pattern for log messages. */
    private static final SimpleDateFormat LONG_DATE_FMT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    /**
     * Short date format pattern for log messages in "quiet" mode.
     * Only time is included since we don't expect "quiet" mode to be used
     * for longer runs.
     */
    private static final SimpleDateFormat SHORT_DATE_FMT = new SimpleDateFormat("HH:mm:ss");

    /** Debug date format. */
    private static final SimpleDateFormat DEBUG_DATE_FMT = new SimpleDateFormat("HH:mm:ss,SSS");

    /** Cached local host address to make sure that every time the same local host is returned. */
    private static InetAddress locHost;

    /** */
    private static volatile long curTimeMillis = System.currentTimeMillis();

    /** Primitive class map. */
    private static final Map<String, Class<?>> primitiveMap = new HashMap<>(16, .5f);

    /** Boxed class map. */
    private static final Map<Class<?>, Class<?>> boxedClsMap = new HashMap<>(16, .5f);

    /** Class loader used to load GridGain. */
    private static final ClassLoader gridClassLoader = GridUtils.class.getClassLoader();

    /** MAC OS invalid argument socket error message. */
    public static final String MAC_INVALID_ARG_MSG = "On MAC OS you may have too many file descriptors open " +
        "(simple restart usually solves the issue)";

    /** Default help pages. */
    public static final List<String> DFLT_HELP_LINKS = Arrays.asList(
        "Troubleshooting:      http://bit.ly/GridGain-Troubleshooting",
        "Documentation Center: http://bit.ly/GridGain-Documentation");

    /** Portable classes. */
    private static final Collection<Class<?>> PORTABLE_CLS = new HashSet<>();

    /** GridGain Logging Directory. */
    public static final String GRIDGAIN_LOG_DIR = System.getenv(GG_LOG_DIR);

    /** GridGain Work Directory. */
    public static final String GRIDGAIN_WORK_DIR = System.getenv(GG_WORK_DIR);

    /**
     * Initializes enterprise check.
     */
    static {
        String osName = System.getProperty("os.name");

        String osLow = osName.toLowerCase();

        // OS type detection.
        if (osLow.contains("win")) {
            if (osLow.contains("95"))
                win95 = true;
            else if (osLow.contains("98"))
                win98 = true;
            else if (osLow.contains("nt"))
                winNt = true;
            else if (osLow.contains("2000"))
                win2k = true;
            else if (osLow.contains("vista"))
                winVista = true;
            else if (osLow.contains("xp"))
                winXp = true;
            else if (osLow.contains("2003"))
                win2003 = true;
            else if (osLow.contains("2008"))
                win2008 = true;
            else if (osLow.contains("7"))
                win7 = true;
            else if (osLow.contains("8.1"))
                win81 = true;
            else if (osLow.contains("8"))
                win8 = true;
            else
                unknownWin = true;
        }
        else if (osLow.contains("netware"))
            netware = true;
        else if (osLow.contains("mac os"))
            mac = true;
        else {
            // UNIXs flavors tokens.
            for (CharSequence os : new String[]{"ix", "inux", "olaris", "un", "ux", "sco", "bsd", "att"})
                if (osLow.contains(os)) {
                    unix = true;

                    break;
                }

            // UNIX name detection.
            if (osLow.contains("olaris"))
                solaris = true;
            else if (osLow.contains("inux"))
                linux = true;
        }

        String osArch = System.getProperty("os.arch");

        String archStr = osArch.toLowerCase();

        // OS architecture detection.
        if (archStr.contains("x86"))
            x86 = true;
        else if (archStr.contains("sparc"))
            sparc = true;

        String javaRtName = System.getProperty("java.runtime.name");
        String javaRtVer = System.getProperty("java.runtime.version");
        String jdkVendor = System.getProperty("java.specification.vendor");
        String jdkName = System.getProperty("java.specification.name");
        String jdkVer = System.getProperty("java.specification.version");
        String osVer = System.getProperty("os.version");
        String jvmSpecName = System.getProperty("java.vm.specification.name");
        String jvmImplVer = System.getProperty("java.vm.version");
        String jvmImplVendor = System.getProperty("java.vm.vendor");
        String jvmImplName = System.getProperty("java.vm.name");

        String jdkStr = javaRtName + ' ' + javaRtVer + ' ' + jvmImplVendor + ' ' + jvmImplName + ' ' +
            jvmImplVer;

        osStr = osName + ' ' + osVer + ' ' + osArch;
        osJdkStr = osLow + ", " + jdkStr;

        // Copy auto variables to static ones.
        GridUtils.osName = osName;
        GridUtils.jdkName = jdkName;
        GridUtils.jdkVendor = jdkVendor;
        GridUtils.jdkVer = jdkVer;
        GridUtils.jdkStr = jdkStr;
        GridUtils.osVer = osVer;
        GridUtils.osArch = osArch;
        GridUtils.jvmSpecName = jvmSpecName;
        GridUtils.jvmImplVer = jvmImplVer;
        GridUtils.jvmImplVendor = jvmImplVendor;
        GridUtils.jvmImplName = jvmImplName;
        GridUtils.javaRtName = javaRtName;
        GridUtils.javaRtVer = javaRtVer;

        primitiveMap.put("byte", byte.class);
        primitiveMap.put("short", short.class);
        primitiveMap.put("int", int.class);
        primitiveMap.put("long", long.class);
        primitiveMap.put("float", float.class);
        primitiveMap.put("double", double.class);
        primitiveMap.put("char", char.class);
        primitiveMap.put("boolean", boolean.class);

        boxedClsMap.put(byte.class, Byte.class);
        boxedClsMap.put(short.class, Short.class);
        boxedClsMap.put(int.class, Integer.class);
        boxedClsMap.put(long.class, Long.class);
        boxedClsMap.put(float.class, Float.class);
        boxedClsMap.put(double.class, Double.class);
        boxedClsMap.put(char.class, Character.class);
        boxedClsMap.put(boolean.class, Boolean.class);

        try {
            OBJECT_CTOR = Object.class.getConstructor();
        }
        catch (NoSuchMethodException e) {
            throw withCause(new AssertionError("Object class does not have empty constructor (is JDK corrupted?)."), e);
        }

        // Constructor factory.
        Method ctorFac = null;
        Object refFac = null;

        try {
            Class<?> refFactoryCls = Class.forName("sun.reflect.ReflectionFactory");

            refFac = refFactoryCls.getMethod("getReflectionFactory").invoke(null);

            ctorFac = refFac.getClass().getMethod("newConstructorForSerialization", Class.class,
                Constructor.class);
        }
        catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException ignored) {
            // No-op.
        }

        CTOR_FACTORY = ctorFac;
        SUN_REFLECT_FACTORY = refFac;

        // Disable hostname SSL verification for development and testing with self-signed certificates.
        if (Boolean.parseBoolean(System.getProperty(GG_DISABLE_HOSTNAME_VERIFIER))) {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override public boolean verify(String hostname, SSLSession sslSes) {
                    return true;
                }
            });
        }

        // Event names initialization.
        for (Field field : GridEventType.class.getFields()) {
            if (field.getType().equals(int.class)) {
                try {
                    assert field.getName().startsWith("EVT_") : "Invalid event name (should start with 'EVT_': " +
                        field.getName();

                    int type = field.getInt(null);

                    String prev = GRID_EVT_NAMES.put(type, field.getName().substring(4));

                    // Check for duplicate event types.
                    assert prev == null : "Duplicate event [type=" + type + ", name1=" + prev +
                        ", name2=" + field.getName() + ']';
                }
                catch (IllegalAccessException e) {
                    throw new GridRuntimeException(e);
                }
            }
        }

        // Event array initialization.
        GRID_EVTS = toIntArray(GRID_EVT_NAMES.keySet());

        // Sort for fast event lookup.
        Arrays.sort(GRID_EVTS);

        // We need to re-initialize EVTS_ALL and EVTS_ALL_MINUS_METRIC_UPDATE
        // because they may have been initialized to null before GRID_EVTS were initialized.
        if (EVTS_ALL == null || EVTS_ALL_MINUS_METRIC_UPDATE == null) {
            try {
                Field f1 = GridEventType.class.getDeclaredField("EVTS_ALL");
                Field f2 = GridEventType.class.getDeclaredField("EVTS_ALL_MINUS_METRIC_UPDATE");

                assert f1 != null;
                assert f2 != null;

                // We use unsafe operations to update static fields on interface because
                // they are treated as static final and cannot be updated via standard reflection.
                UNSAFE.putObjectVolatile(UNSAFE.staticFieldBase(f1), UNSAFE.staticFieldOffset(f1), gridEvents());
                UNSAFE.putObjectVolatile(UNSAFE.staticFieldBase(f2), UNSAFE.staticFieldOffset(f2),
                    gridEvents(EVT_NODE_METRICS_UPDATED));

                assert EVTS_ALL != null;
                assert EVTS_ALL.length == GRID_EVTS.length;

                assert EVTS_ALL_MINUS_METRIC_UPDATE != null;
                assert EVTS_ALL_MINUS_METRIC_UPDATE.length == GRID_EVTS.length - 1;

                // Validate correctness.
                for (int type : GRID_EVTS) {
                    assert containsIntArray(EVTS_ALL, type);

                    if (type != EVT_NODE_METRICS_UPDATED)
                        assert containsIntArray(EVTS_ALL_MINUS_METRIC_UPDATE, type);
                }

                assert !containsIntArray(EVTS_ALL_MINUS_METRIC_UPDATE, EVT_NODE_METRICS_UPDATED);
            }
            catch (NoSuchFieldException e) {
                throw new GridRuntimeException(e);
            }
        }

        Thread timer = new Thread(new Runnable() {
            @SuppressWarnings({"BusyWait", "InfiniteLoopStatement"})
            @Override public void run() {
                while (true) {
                    curTimeMillis = System.currentTimeMillis();

                    try {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException ignored) {
                        U.log(null, "Timer thread has been interrupted.");

                        break;
                    }
                }
            }
        }, "gridgain-clock");

        timer.setDaemon(true);

        timer.setPriority(10);

        timer.start();

        PORTABLE_CLS.add(Byte.class);
        PORTABLE_CLS.add(Short.class);
        PORTABLE_CLS.add(Integer.class);
        PORTABLE_CLS.add(Long.class);
        PORTABLE_CLS.add(Float.class);
        PORTABLE_CLS.add(Double.class);
        PORTABLE_CLS.add(Character.class);
        PORTABLE_CLS.add(Boolean.class);
        PORTABLE_CLS.add(String.class);
        PORTABLE_CLS.add(UUID.class);
        PORTABLE_CLS.add(Date.class);
        PORTABLE_CLS.add(Timestamp.class);
        PORTABLE_CLS.add(byte[].class);
        PORTABLE_CLS.add(short[].class);
        PORTABLE_CLS.add(int[].class);
        PORTABLE_CLS.add(long[].class);
        PORTABLE_CLS.add(float[].class);
        PORTABLE_CLS.add(double[].class);
        PORTABLE_CLS.add(char[].class);
        PORTABLE_CLS.add(boolean[].class);
        PORTABLE_CLS.add(String[].class);
        PORTABLE_CLS.add(UUID[].class);
        PORTABLE_CLS.add(Date[].class);
        PORTABLE_CLS.add(Timestamp[].class);
    }

    /**
     * @return System time approximated by 10 ms.
     */
    public static long currentTimeMillis() {
        return curTimeMillis;
    }

    /**
     * @return Value of {@link System#nanoTime()} in microseconds.
     */
    public static long microTime() {
        return System.nanoTime() / 1000;
    }

    /**
     * Gets nearest power of 2 larger or equal than v.
     *
     * @param v Value.
     * @return Nearest power of 2.
     */
    public static int ceilPow2(int v) {
        v--;

        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;

        return ++v;
    }

    /**
     * @param i Value.
     * @return {@code true} If the given value is power of 2 (0 is not power of 2).
     */
    public static boolean isPow2(int i) {
        return i > 0 && (i & (i - 1)) == 0;
    }

    /**
     * Return SUN specific constructor factory.
     *
     * @return SUN specific constructor factory.
     */
    @Nullable public static Method ctorFactory() {
        return CTOR_FACTORY;
    }

    /**
     * @return Empty constructor for object class.
     */
    public static Constructor objectConstructor() {
        return OBJECT_CTOR;
    }

    /**
     * SUN JDK specific reflection factory for objects without public constructor.
     *
     * @return Reflection factory for objects without public constructor.
     */
    @Nullable public static Object sunReflectionFactory() {
        return SUN_REFLECT_FACTORY;
    }

    /**
     * Gets name for given grid event type.
     *
     * @param type Event type.
     * @return Event name.
     */
    public static String gridEventName(int type) {
        String name = GRID_EVT_NAMES.get(type);

        return name != null ? name : Integer.toString(type);
    }

    /**
     * Gets all event types.
     *
     * @param excl Optional exclude events.
     * @return All events minus excluded ones.
     */
    public static int[] gridEvents(final int... excl) {
        if (F.isEmpty(excl))
            return GRID_EVTS;

        List<Integer> evts = toIntList(GRID_EVTS, new P1<Integer>() {
            @Override public boolean apply(Integer i) {
                return !containsIntArray(excl, i);
            }
        });

        return toIntArray(evts);
    }

    /**
     * @param discoSpi Discovery SPI.
     * @return {@code True} if ordering is supported.
     */
    public static boolean discoOrdered(GridDiscoverySpi discoSpi) {
        GridDiscoverySpiOrderSupport ann = U.getAnnotation(discoSpi.getClass(), GridDiscoverySpiOrderSupport.class);

        return ann != null && ann.value();
    }

    /**
     * @return Checks if disco ordering should be enforced.
     */
    public static boolean relaxDiscoveryOrdered() {
        return "true".equalsIgnoreCase(System.getProperty(GG_NO_DISCO_ORDER));
    }

    /**
     * This method should be used for adding quick debug statements in code
     * while debugging. Calls to this method should never be committed to master.
     *
     * @param msg Message to debug.
     * @deprecated Calls to this method should never be committed to master.
     */
    @Deprecated
    public static void debug(Object msg) {
        X.println(debugPrefix() + msg);
    }

    /**
     * This method should be used for adding quick debug statements in code
     * while debugging. Calls to this method should never be committed to master.
     *
     * @param msg Message to debug.
     * @deprecated Calls to this method should never be committed to master.
     */
    @Deprecated
    public static void debugx(String msg) {
        X.printerrln(debugPrefix() + msg);
    }

    /**
     * This method should be used for adding quick debug statements in code
     * while debugging. Calls to this method should never be committed to master.
     *
     * @param log Logger.
     * @param msg Message to debug.
     *
     * @deprecated Calls to this method should never be committed to master.
     */
    @Deprecated
    public static void debug(GridLogger log, String msg) {
        log.info(msg);
    }

    /**
     * Prints stack trace of the current thread to {@code System.out}.
     *
     * @deprecated Calls to this method should never be committed to master.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public static void dumpStack() {
        dumpStack("Dumping stack.");
    }

    /**
     * Prints stack trace of the current thread to {@code System.out}.
     *
     * @param msg Message to print with the stack.
     *
     * @deprecated Calls to this method should never be committed to master.
     */
    @Deprecated
    public static void dumpStack(String msg) {
        new Exception(debugPrefix() + msg).printStackTrace(System.out);
    }

    /**
     * @param log Logger.
     * @param msg Message.
     */
    public static void dumpStack(@Nullable GridLogger log, String msg) {
        U.error(log, "Dumping stack.", new Exception(msg));
    }

    /**
     * Prints stack trace of the current thread to provided output stream.
     *
     * @param msg Message to print with the stack.
     * @param out Output to dump stack to.
     *
     * @deprecated Calls to this method should never be committed to master.
     */
    @Deprecated
    public static void dumpStack(String msg, PrintStream out) {
        new Exception(msg).printStackTrace(out);
    }

    /**
     * Prints stack trace of the current thread to provided logger.
     *
     * @param log Logger.
     * @param msg Message to print with the stack.
     *
     * @deprecated Calls to this method should never be committed to master.
     */
    @Deprecated
    public static void debugStack(GridLogger log, String msg) {
        log.error(msg, new Exception(debugPrefix() + msg));
    }

    /**
     * @return Common prefix for debug messages.
     */
    private static String debugPrefix() {
        return '<' + DEBUG_DATE_FMT.format(new Date(System.currentTimeMillis())) + "><DEBUG><" +
            Thread.currentThread().getName() + '>' + ' ';
    }

    /**
     * Prints heap usage.
     */
    public static void debugHeapUsage() {
        System.gc();

        Runtime runtime = Runtime.getRuntime();

        X.println('<' + DEBUG_DATE_FMT.format(new Date(System.currentTimeMillis())) + "><DEBUG><" +
            Thread.currentThread().getName() + "> Heap stats [free=" + runtime.freeMemory() / (1024 * 1024) +
            "M, total=" + runtime.totalMemory() / (1024 * 1024) + "M]");
    }

    /**
     * Gets heap size in GB rounded to specified precision.
     *
     * @param node Node.
     * @param precision Precision.
     * @return Heap size in GB.
     */
    public static double heapSize(GridNode node, int precision) {
        return heapSize(Collections.singleton(node), precision);
    }

    /**
     * Gets total heap size in GB rounded to specified precision.
     *
     * @param nodes Nodes.
     * @param precision Precision.
     * @return Total heap size in GB.
     */
    public static double heapSize(Iterable<GridNode> nodes, int precision) {
        // In bytes.
        double heap = 0.0;

        for (GridNode n : nodesPerJvm(nodes)) {
            GridNodeMetrics m = n.metrics();

            heap += Math.max(m.getHeapMemoryInitialized(), m.getHeapMemoryMaximum());
        }

        return roundedHeapSize(heap, precision);
    }

    /**
     * Returns one representative node for each JVM.
     *
     * @param nodes Nodes.
     * @return Collection which contains only one representative node for each JVM.
     */
    private static Iterable<GridNode> nodesPerJvm(Iterable<GridNode> nodes) {
        Map<String, GridNode> grpMap = new HashMap<>();

        // Group by mac addresses and pid.
        for (GridNode node : nodes) {
            String grpId = node.attribute(ATTR_MACS) + "|" + node.attribute(ATTR_JVM_PID);

            if (!grpMap.containsKey(grpId))
                grpMap.put(grpId, node);
        }

        return grpMap.values();
    }

    /**
     * Returns current JVM maxMemory in the same format as {@link #heapSize(GridNode, int)}.
     *
     * @param precision Precision.
     * @return Maximum memory size in GB.
     */
    public static double heapSize(int precision) {
        return roundedHeapSize(Runtime.getRuntime().maxMemory(), precision);
    }

    /**
     * Rounded heap size in gigabytes.
     *
     * @param heap Heap.
     * @param precision Precision.
     * @return Rounded heap size.
     */
    private static double roundedHeapSize(double heap, int precision) {
        double rounded = new BigDecimal(heap / (1024 * 1024 * 1024d)).round(new MathContext(precision)).doubleValue();

        return rounded < 0.1 ? 0.1 : rounded;
    }

    /**
     * Performs thread dump and prints all available info to the given log.
     *
     * @param log Logger.
     */
    public static void dumpThreads(@Nullable GridLogger log) {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

        ThreadInfo[] threadInfos =
            mxBean.dumpAllThreads(mxBean.isObjectMonitorUsageSupported(), mxBean.isSynchronizerUsageSupported());

        GridStringBuilder sb = new GridStringBuilder("Thread dump at ")
            .a(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z").format(new Date(U.currentTimeMillis()))).a(NL);

        for (ThreadInfo info : threadInfos) {
            printThreadInfo(info, sb);

            sb.a(NL);

            if (info.getLockedSynchronizers() != null && info.getLockedSynchronizers().length > 0) {
                printSynchronizersInfo(info.getLockedSynchronizers(), sb);

                sb.a(NL);
            }
        }

        sb.a(NL);

        warn(log, sb.toString());
    }

    /**
     * Prints single thread info to a buffer.
     *
     * @param threadInfo Thread info.
     * @param sb Buffer.
     */
    private static void printThreadInfo(ThreadInfo threadInfo, GridStringBuilder sb) {
        sb.a("Thread [name=\"").a(threadInfo.getThreadName())
            .a("\", id=").a(threadInfo.getThreadId())
            .a(", state=").a(threadInfo.getThreadState())
            .a(", blockCnt=").a(threadInfo.getBlockedCount())
            .a(", waitCnt=").a(threadInfo.getWaitedCount()).a("]").a(NL);

        LockInfo lockInfo = threadInfo.getLockInfo();

        if (lockInfo != null) {
            sb.a("    Lock [object=").a(lockInfo)
                .a(", ownerName=").a(threadInfo.getLockOwnerName())
                .a(", ownerId=").a(threadInfo.getLockOwnerId()).a("]").a(NL);
        }

        MonitorInfo[] monitors = threadInfo.getLockedMonitors();
        StackTraceElement[] elements = threadInfo.getStackTrace();

        for (int i = 0; i < elements.length; i++) {
            StackTraceElement e = elements[i];

            sb.a("        at ").a(e.toString());

            for (MonitorInfo monitor : monitors) {
                if (monitor.getLockedStackDepth() == i)
                    sb.a(NL).a("        - locked ").a(monitor);
            }

            sb.a(NL);
        }
    }

    /**
     * Prints Synchronizers info to a buffer.
     *
     * @param syncs Synchronizers info.
     * @param sb Buffer.
     */
    private static void printSynchronizersInfo(LockInfo[] syncs, GridStringBuilder sb) {
        sb.a("    Locked synchronizers:");

        for (LockInfo info : syncs)
            sb.a(NL).a("        ").a(info);
    }

    /**
     * Gets empty constructor for class even if the class does not have empty constructor
     * declared. This method is guaranteed to work with SUN JDK and other JDKs still need
     * to be tested.
     *
     * @param cls Class to get empty constructor for.
     * @return Empty constructor if one could be found or {@code null} otherwise.
     * @throws GridException If failed.
     */
    @Nullable public static Constructor<?> forceEmptyConstructor(Class<?> cls) throws GridException {
        Constructor<?> ctor = null;

        try {
            return cls.getDeclaredConstructor();
        }
        catch (Exception ignore) {
            Method ctorFac = U.ctorFactory();
            Object sunRefFac = U.sunReflectionFactory();

            if (ctorFac != null && sunRefFac != null)
                try {
                    ctor = (Constructor)ctorFac.invoke(sunRefFac, cls, U.objectConstructor());
                }
                catch (IllegalAccessException | InvocationTargetException e) {
                    throw new GridException("Failed to get object constructor for class: " + cls, e);
                }
        }

        return ctor;
    }

    /**
     * Creates new instance of a class only if it has an empty constructor (can be non-public).
     *
     * @param cls Class to instantiate.
     * @return New instance of the class or {@code null} if empty constructor could not be assigned.
     * @throws GridException If failed.
     */
    @Nullable public static <T> T newInstance(Class<T> cls) throws GridException {
        boolean set = false;

        Constructor<T> ctor = null;

        try {
            ctor = cls.getDeclaredConstructor();

            if (ctor == null)
                return null;

            if (!ctor.isAccessible()) {
                ctor.setAccessible(true);

                set = true;
            }

            return ctor.newInstance();
        }
        catch (NoSuchMethodException e) {
            throw new GridException("Failed to find empty constructor for class: " + cls, e);
        }
        catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new GridException("Failed to create new instance for class: " + cls, e);
        } finally {
            if (ctor != null && set)
                ctor.setAccessible(false);
        }
    }

    /**
     * Creates new instance of a class even if it does not have public constructor.
     *
     * @param cls Class to instantiate.
     * @return New instance of the class or {@code null} if empty constructor could not be assigned.
     * @throws GridException If failed.
     */
    @SuppressWarnings({"unchecked"})
    @Nullable public static <T> T forceNewInstance(Class<?> cls) throws GridException {
        Constructor ctor = forceEmptyConstructor(cls);

        if (ctor == null)
            return null;

        boolean set = false;

        try {

            if (!ctor.isAccessible()) {
                ctor.setAccessible(true);

                set = true;
            }

            return (T)ctor.newInstance();
        }
        catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new GridException("Failed to create new instance for class: " + cls, e);
        } finally {
            if (set)
                ctor.setAccessible(false);
        }
    }

    /**
     * Pretty-formatting for minutes.
     *
     * @param mins Minutes to format.
     * @return Formatted presentation of minutes.
     */
    public static String formatMins(long mins) {
        assert mins >= 0;

        if (mins == 0)
            return "< 1 min";

        SB sb = new SB();

        long dd = mins / 1440; // 1440 mins = 60 mins * 24 hours

        if (dd > 0)
            sb.a(dd).a(dd == 1 ? " day " : " days ");

        mins %= 1440;

        long hh = mins / 60;

        if (hh > 0)
            sb.a(hh).a(hh == 1 ? " hour " : " hours ");

        mins %= 60;

        if (mins > 0)
            sb.a(mins).a(mins == 1 ? " min " : " mins ");

        return sb.toString().trim();
    }

    /**
     * Gets 8-character substring of UUID (for terse logging).
     *
     * @param id Input ID.
     * @return 8-character ID substring.
     */
    public static String id8(UUID id) {
        return id.toString().substring(0, 8);
    }

    /**
     * Gets 8-character substring of {@link GridUuid} (for terse logging).
     * The ID8 will be constructed as follows:
     * <ul>
     * <li>Take first 4 digits for global ID, i.e. {@code GridUuid.globalId()}.</li>
     * <li>Take last 4 digits for local ID, i.e. {@code GridUuid.localId()}.</li>
     * </ul>
     *
     * @param id Input ID.
     * @return 8-character representation of {@code GridUuid}.
     */
    public static String id8(GridUuid id) {
        String s = id.toString();

        return s.substring(0, 4) + s.substring(s.length() - 4);
    }

    /**
     *
     * @param len Number of characters to fill in.
     * @param ch Character to fill with.
     * @return String.
     */
    public static String filler(int len, char ch) {
        char[] a = new char[len];

        Arrays.fill(a, ch);

        return new String(a);
    }

    /**
     * Writes array to output stream.
     *
     * @param out Output stream.
     * @param arr Array to write.
     * @param <T> Array type.
     * @throws IOException If failed.
     */
    public static <T> void writeArray(ObjectOutput out, T[] arr) throws IOException {
        int len = arr == null ? 0 : arr.length;

        out.writeInt(len);

        if (arr != null && arr.length > 0)
            for (T t : arr)
                out.writeObject(t);
    }

    /**
     * Reads array from input stream.
     *
     * @param in Input stream.
     * @return Deserialized array.
     * @throws IOException If failed.
     * @throws ClassNotFoundException If class not found.
     */
    @Nullable public static Object[] readArray(ObjectInput in) throws IOException, ClassNotFoundException {
        int len = in.readInt();

        Object[] arr = null;

        if (len > 0) {
            arr = new Object[len];

            for (int i = 0; i < len; i++)
                arr[i] = in.readObject();
        }

        return arr;
    }

    /**
     * Reads array from input stream.
     *
     * @param in Input stream.
     * @return Deserialized array.
     * @throws IOException If failed.
     * @throws ClassNotFoundException If class not found.
     */
    @SuppressWarnings("unchecked")
    @Nullable public static <K, V> GridPredicate<GridCacheEntry<K, V>>[] readEntryFilterArray(ObjectInput in)
        throws IOException, ClassNotFoundException {
        int len = in.readInt();

        GridPredicate<GridCacheEntry<K, V>>[] arr = null;

        if (len > 0) {
            arr = new GridPredicate[len];

            for (int i = 0; i < len; i++)
                arr[i] = (GridPredicate<GridCacheEntry<K, V>>)in.readObject();
        }

        return arr;
    }

    /**
     *
     * @param out Output.
     * @param col Set to write.
     * @throws IOException If write failed.
     */
    public static void writeCollection(ObjectOutput out, Collection<?> col) throws IOException {
        if (col != null) {
            out.writeInt(col.size());

            for (Object o : col)
                out.writeObject(o);
        }
        else
            out.writeInt(-1);
    }

    /**
     * Writes collection of byte arrays to data output.
     *
     * @param out Output to write to.
     * @param bytes Collection with byte arrays.
     * @throws IOException If write failed.
     */
    public static void writeBytesCollection(DataOutput out, Collection<byte[]> bytes) throws IOException {
        if (bytes != null) {
            out.writeInt(bytes.size());

            for (byte[] b : bytes)
                writeByteArray(out, b);
        }
        else
            out.writeInt(-1);
    }

    /**
     * Reads collection of byte arrays from data input.
     *
     * @param in Data input to read from.
     * @return List of byte arrays.
     * @throws IOException If read failed.
     */
    public static List<byte[]> readBytesList(DataInput in) throws IOException {
        int size = in.readInt();

        if (size < 0)
            return null;

        List<byte[]> res = new ArrayList<>(size);

        for (int i = 0; i < size; i++)
            res.add(readByteArray(in));

        return res;
    }

    /**
     *
     * @param out Output.
     * @param col Set to write.
     * @throws IOException If write failed.
     */
    public static void writeIntCollection(DataOutput out, Collection<Integer> col) throws IOException {
        if (col != null) {
            out.writeInt(col.size());

            for (Integer i : col)
                out.writeInt(i);
        }
        else
            out.writeInt(-1);
    }

    /**
     * @param in Input.
     * @return Deserialized set.
     * @throws IOException If deserialization failed.
     * @throws ClassNotFoundException If deserialized class could not be found.
     */
    @Nullable public static <E> Collection<E> readCollection(ObjectInput in)
        throws IOException, ClassNotFoundException {
        return readList(in);
    }

    /**
     * @param in Input.
     * @return Deserialized set.
     * @throws IOException If deserialization failed.
     */
    @Nullable public static Collection<Integer> readIntCollection(DataInput in) throws IOException {
        int size = in.readInt();

        // Check null flag.
        if (size == -1)
            return null;

        Collection<Integer> col = new ArrayList<>(size);

        for (int i = 0; i < size; i++)
            col.add(in.readInt());

        return col;
    }

    /**
     *
     * @param m Map to copy.
     * @param <K> Key type.
     * @param <V> Value type
     * @return Copied map.
     */
    public static <K, V> Map<K, V> copyMap(Map<K, V> m) {
        return new HashMap<>(m);
    }

    /**
     *
     * @param m Map to seal.
     * @param <K> Key type.
     * @param <V> Value type
     * @return Sealed map.
     */
    public static <K, V> Map<K, V> sealMap(Map<K, V> m) {
        assert m != null;

        return Collections.unmodifiableMap(new HashMap<>(m));
    }

    /**
     * Seal collection.
     *
     * @param c Collection to seal.
     * @param <E> Entry type
     * @return Sealed collection.
     */
    public static <E> List<E> sealList(Collection<E> c) {
        return Collections.unmodifiableList(new ArrayList<>(c));
    }

    /**
     * Convert array to seal list.
     *
     * @param a Array for convert to seal list.
     * @param <E> Entry type
     * @return Sealed collection.
     */
    public static <E> List<E> sealList(E... a) {
        return Collections.unmodifiableList(Arrays.asList(a));
    }

    /**
     * Gets display name of the network interface this IP address belongs to.
     *
     * @param addr IP address for which to find network interface name.
     * @return Network interface name or {@code null} if can't be found.
     */
    @Nullable public static String getNetworkInterfaceName(String addr) {
        assert addr != null;

        try {
            InetAddress inetAddr = InetAddress.getByName(addr);

            for (NetworkInterface itf : asIterable(NetworkInterface.getNetworkInterfaces()))
                for (InetAddress itfAddr : asIterable(itf.getInetAddresses()))
                    if (itfAddr.equals(inetAddr))
                        return itf.getDisplayName();
        }
        catch (UnknownHostException ignore) {
            return null;
        }
        catch (SocketException ignore) {
            return null;
        }

        return null;
    }

    /**
     * Tries to resolve host by name, returning local host if input is empty.
     * This method reflects how {@link GridConfiguration#getLocalHost()} should
     * be handled in most places.
     *
     * @param hostName Hostname or {@code null} if local host should be returned.
     * @return Address of given host or of localhost.
     * @throws IOException If attempt to get local host failed.
     */
    public static InetAddress resolveLocalHost(@Nullable String hostName) throws IOException {
        return F.isEmpty(hostName) ?
            // Should default to InetAddress#anyLocalAddress which is package-private.
            new InetSocketAddress(0).getAddress() :
            InetAddress.getByName(hostName);
    }

    /**
     * Determines whether current local host is different from previously cached.
     *
     * @return {@code true} or {@code false} depending on whether or not local host
     *      has changed from the cached value.
     * @throws IOException If attempt to get local host failed.
     */
    public static synchronized boolean isLocalHostChanged() throws IOException {
        InetAddress locHost0 = locHost;

        return locHost0 != null && !resetLocalHost().equals(locHost0);
    }

    /**
     * Returns host names consistent with {@link #resolveLocalHost(String)}. So when it returns
     * a common address this method returns single host name, and when a wildcard address passed
     * this method tries to collect addresses of all available interfaces.
     *
     * @param locAddr Local address to resolve.
     * @return Resolved available addresses of given local address.
     * @throws IOException If failed.
     * @throws GridException If no network interfaces found.
     */
    public static GridBiTuple<Collection<String>, Collection<String>> resolveLocalAddresses(InetAddress locAddr)
        throws IOException, GridException {
        assert locAddr != null;

        Collection<String> addrs = new ArrayList<>();
        Collection<String> hostNames = new ArrayList<>();

        if (locAddr.isAnyLocalAddress()) {
            // It should not take longer than 2 seconds to reach
            // local address on any network.
            int reachTimeout = 2000;

            for (NetworkInterface itf : asIterable(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress addr : asIterable(itf.getInetAddresses())) {
                    if (!addr.isLinkLocalAddress() && reachable(itf, addr, reachTimeout))
                        addresses(addr, addrs, hostNames);
                }
            }

            if (F.isEmpty(addrs))
                throw new GridException("No network addresses found (is networking enabled?).");
        }
        else
            addresses(locAddr, addrs, hostNames);

        return F.t(addrs, hostNames);
    }

    /**
     * @param addr Address.
     * @param addrs Addresses.
     * @param hostNames Host names.
     */
    private static void addresses(InetAddress addr, Collection<String> addrs, Collection<String> hostNames) {
        String hostName = addr.getHostName();

        String ipAddr = addr.getHostAddress();

        hostName = F.isEmpty(hostName) || hostName.equals(ipAddr) || addr.isLoopbackAddress() ? "" : hostName;

        addrs.add(ipAddr);
        hostNames.add(hostName);
    }

    /**
     * Gets local host. Implementation will first attempt to get a non-loopback
     * address. If that fails, then loopback address will be returned.
     * <p>
     * Note that this method is synchronized to make sure that local host
     * initialization happens only once.
     *
     * @return Address representing local host.
     * @throws IOException If attempt to get local host failed.
     */
    public static synchronized InetAddress getLocalHost() throws IOException {
        if (locHost == null)
            // Cache it.
            resetLocalHost();

        return locHost;
    }

    /**
     * @return Local host.
     * @throws IOException If attempt to get local host failed.
     */
    private static synchronized InetAddress resetLocalHost() throws IOException {
        locHost = null;

        String sysLocHost = GridSystemProperties.getString(GG_LOCAL_HOST);

        if (sysLocHost != null)
            sysLocHost = sysLocHost.trim();

        if (!F.isEmpty(sysLocHost))
            locHost = InetAddress.getByName(sysLocHost);
        else {
            List<NetworkInterface> itfs = new ArrayList<>();

            for (NetworkInterface itf : asIterable(NetworkInterface.getNetworkInterfaces()))
                itfs.add(itf);

            Collections.sort(itfs, new Comparator<NetworkInterface>() {
                @Override public int compare(NetworkInterface itf1, NetworkInterface itf2) {
                    // Interfaces whose name starts with 'e' should go first.
                    return itf1.getName().compareTo(itf2.getName());
                }
            });

            // It should not take longer than 2 seconds to reach
            // local address on any network.
            int reachTimeout = 2000;

            for (NetworkInterface itf : itfs) {
                boolean found = false;

                for (InetAddress addr : asIterable(itf.getInetAddresses())) {
                    if (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress() && reachable(itf, addr, reachTimeout)) {
                        locHost = addr;

                        found = true;

                        break;
                    }
                }

                if (found)
                    break;
            }
        }

        if (locHost == null)
            locHost = InetAddress.getLocalHost();

        return locHost;
    }

    /**
     * Checks if address can be reached using three argument InetAddress.isReachable() version.
     *
     * @param itf Network interface to use for test.
     * @param addr Address to check.
     * @param reachTimeout Timeout for the check.
     * @return {@code True} if address is reachable.
     */
    public static boolean reachable(NetworkInterface itf, InetAddress addr, int reachTimeout) {
        try {
            return addr.isReachable(itf, 0, reachTimeout);
        }
        catch (IOException ignore) {
            return false;
        }
    }

    /**
     * Checks if address can be reached using one argument InetAddress.isReachable() version.
     *
     * @param addr Address to check.
     * @param reachTimeout Timeout for the check.
     * @return {@code True} if address is reachable.
     */
    public static boolean reachable(InetAddress addr, int reachTimeout) {
        try {
            return addr.isReachable(reachTimeout);
        }
        catch (IOException ignore) {
            return false;
        }
    }

    /**
     * @param loc Local node.
     * @param rmt Remote node.
     * @return Whether given nodes have the same macs.
     */
    public static boolean sameMacs(GridNode loc, GridNode rmt) {
        assert loc != null;
        assert rmt != null;

        String locMacs = loc.attribute(GridNodeAttributes.ATTR_MACS);
        String rmtMacs = rmt.attribute(GridNodeAttributes.ATTR_MACS);

        return locMacs != null && locMacs.equals(rmtMacs);
    }

    /**
     * Gets a list of all local non-loopback IPs known to this JVM.
     * Note that this will include both IPv4 and IPv6 addresses (even if one "resolves"
     * into another). Loopbacks will be skipped.
     *
     * @return List of all known local IPs (empty list if no addresses available).
     */
    public static synchronized Collection<String> allLocalIps() {
        List<String> ips = new ArrayList<>(4);

        try {
            Enumeration<NetworkInterface> itfs = NetworkInterface.getNetworkInterfaces();

            if (itfs != null) {
                for (NetworkInterface itf : asIterable(itfs)) {
                    if (!itf.isLoopback()) {
                        Enumeration<InetAddress> addrs = itf.getInetAddresses();

                        if (addrs != null) {
                            for (InetAddress addr : asIterable(addrs)) {
                                String hostAddr = addr.getHostAddress();

                                if (!addr.isLoopbackAddress() && !ips.contains(hostAddr))
                                    ips.add(hostAddr);
                            }
                        }
                    }
                }
            }
        }
        catch (SocketException ignore) {
            return Collections.emptyList();
        }

        Collections.sort(ips);

        return ips;
    }

    /**
     * Gets a list of all local enabled MACs known to this JVM. It
     * is using hardware address of the network interface that is not guaranteed to be
     * MAC addresses (but in most cases it is).
     * <p>
     * Note that if network interface is disabled - its MAC won't be included. All
     * local network interfaces are probed including loopbacks. Virtual interfaces
     * (sub-interfaces) are skipped.
     * <p>
     * Note that on linux getHardwareAddress() can return null from time to time
     * if NetworkInterface.getHardwareAddress() method is called from many threads.
     *
     * @return List of all known enabled local MACs or empty list
     *      if no MACs could be found.
     */
    public static synchronized Collection<String> allLocalMACs() {
        List<String> macs = new ArrayList<>(3);

        try {
            Enumeration<NetworkInterface> itfs = NetworkInterface.getNetworkInterfaces();

            if (itfs != null) {
                for (NetworkInterface itf : asIterable(itfs)) {
                    byte[] hwAddr = itf.getHardwareAddress();

                    // Loopback produces empty MAC.
                    if (hwAddr != null && hwAddr.length > 0) {
                        String mac = byteArray2HexString(hwAddr);

                        if (!macs.contains(mac))
                            macs.add(mac);
                    }
                }
            }
        }
        catch (SocketException ignore) {
            return Collections.emptyList();
        }

        Collections.sort(macs);

        return macs;
    }

    /**
     * Downloads resource by URL.
     *
     * @param url URL to download.
     * @param file File where downloaded resource should be stored.
     * @return File where downloaded resource should be stored.
     * @throws IOException If error occurred.
     */
    public static File downloadUrl(URL url, File file) throws IOException {
        assert url != null;
        assert file != null;

        InputStream in = null;
        OutputStream out = null;

        try {
            URLConnection conn = url.openConnection();

            if (conn instanceof HttpsURLConnection) {
                HttpsURLConnection https = (HttpsURLConnection)conn;

                https.setHostnameVerifier(new DeploymentHostnameVerifier());

                SSLContext ctx = SSLContext.getInstance(HTTPS_PROTOCOL);

                ctx.init(null, getTrustManagers(), null);

                // Initialize socket factory.
                https.setSSLSocketFactory(ctx.getSocketFactory());
            }

            in = conn.getInputStream();

            if (in == null)
                throw new IOException("Failed to open connection: " + url.toString());

            out = new BufferedOutputStream(new FileOutputStream(file));

            copy(in, out);
        }
        catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IOException("Failed to open HTTPs connection [url=" + url.toString() + ", msg=" + e + ']', e);
        } finally {
            close(in, null);
            close(out, null);
        }

        return file;
    }

    /**
     * Construct array with one trust manager which don't reject input certificates.
     *
     * @return Array with one X509TrustManager implementation of trust manager.
     */
    private static TrustManager[] getTrustManagers() {
        return new TrustManager[]{
            new X509TrustManager() {
                @Nullable @Override public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    /* No-op. */
                }

                @Override public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    /* No-op. */
                }
            }
        };
    }

    /**
     * Copies metadata from given object into the passed in metadata aware
     * instance - if object is not {@code null} and implements {@link GridMetadataAware}.
     * Otherwise - returns passed metadata aware instance unchanged.
     *
     * @param t Passed in metadata aware instance.
     * @param obj Object to copy metadata from.
     * @param <T> Type of the metadata aware instance.
     * @return Metadata aware instance with metadata potentially copied.
     */
    public static <T extends GridMetadataAware> T withMeta(T t, @Nullable Object obj) {
        assert t != null;

        if (obj instanceof GridMetadataAware)
            t.copyMeta((GridMetadataAware)obj);

        return t;
    }

    /**
     * Replace password in URI string with a single '*' character.
     * <p>
     * Parses given URI by applying &quot;.*://(.*:.*)@.*&quot;
     * regular expression pattern and than if URI matches it
     * replaces password strings between '/' and '@' with '*'.
     *
     * @param uri URI which password should be replaced.
     * @return Converted URI string
     */
    @Nullable public static String hidePassword(@Nullable String uri) {
        if (uri == null)
            return null;

        if (Pattern.matches(".*://(.*:.*)@.*", uri)) {
            int userInfoLastIdx = uri.indexOf('@');

            assert userInfoLastIdx != -1;

            String str = uri.substring(0, userInfoLastIdx);

            int userInfoStartIdx = str.lastIndexOf('/');

            str = str.substring(userInfoStartIdx + 1);

            String[] params = str.split(";");

            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < params.length; i++) {
                int idx;

                if ((idx = params[i].indexOf(':')) != -1)
                    params[i] = params[i].substring(0, idx + 1) + '*';

                builder.append(params[i]);

                if (i != params.length - 1)
                    builder.append(';');
            }

            return new StringBuilder(uri).replace(userInfoStartIdx + 1, userInfoLastIdx,
                builder.toString()).toString();
        }

        return uri;
    }

    /**
     * @return Class loader used to load GridGain itself.
     */
    public static ClassLoader gridClassLoader() {
        return gridClassLoader;
    }

    /**
     * @param parent Parent to find.
     * @param ldr Loader to check.
     * @return {@code True} if parent found.
     */
    public static boolean hasParent(@Nullable ClassLoader parent, ClassLoader ldr) {
        if (parent != null) {
            for (; ldr != null; ldr = ldr.getParent()) {
                if (ldr.equals(parent))
                    return true;
            }

            return false;
        }

        return true;
    }

    /**
     * Verifier always returns successful result for any host.
     */
    private static class DeploymentHostnameVerifier implements HostnameVerifier {
        /** {@inheritDoc} */
        @Override public boolean verify(String hostname, SSLSession ses) {
            // Remote host trusted by default.
            return true;
        }
    }

    /**
     * Makes a {@code '+---+'} dash line.
     *
     * @param len Length of the dash line to make.
     * @return Dash line.
     */
    public static String dash(int len) {
        char[] dash = new char[len];

        Arrays.fill(dash, '-');

        dash[0] = dash[len - 1] = '+';

        return new String(dash);
    }

    /**
     * Creates space filled string of given length.
     *
     * @param len Number of spaces.
     * @return Space filled string of given length.
     */
    public static String pad(int len) {
        char[] dash = new char[len];

        Arrays.fill(dash, ' ');

        return new String(dash);
    }

    /**
     * Formats system time in milliseconds for printing in logs.
     *
     * @param sysTime System time.
     * @return Formatted time string.
     */
    public static String format(long sysTime) {
        return LONG_DATE_FMT.format(new java.util.Date(sysTime));
    }

    /**
     * Takes given collection, shuffles it and returns iterable instance.
     *
     * @param <T> Type of elements to create iterator for.
     * @param col Collection to shuffle.
     * @return Iterable instance over randomly shuffled collection.
     */
    public static <T> Iterable<T> randomIterable(Collection<T> col) {
        List<T> list = new ArrayList<>(col);

        Collections.shuffle(list);

        return list;
    }

    /**
     * Converts enumeration to iterable so it can be used in {@code foreach} construct.
     *
     * @param <T> Types of instances for iteration.
     * @param e Enumeration to convert.
     * @return Iterable over the given enumeration.
     */
    public static <T> Iterable<T> asIterable(final Enumeration<T> e) {
        return new Iterable<T>() {
            @Override public Iterator<T> iterator() {
                return new Iterator<T>() {
                    @Override public boolean hasNext() {
                        return e.hasMoreElements();
                    }

                    @SuppressWarnings({"IteratorNextCanNotThrowNoSuchElementException"})
                    @Override public T next() {
                        return e.nextElement();
                    }

                    @Override public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Copy source file (or folder) to destination file (or folder). Supported source & destination:
     * <ul>
     * <li>File to File</li>
     * <li>File to Folder</li>
     * <li>Folder to Folder (Copy the content of the directory and not the directory itself)</li>
     * </ul>
     *
     * @param src Source file or folder.
     * @param dest Destination file or folder.
     * @param overwrite Whether or not overwrite existing files and folders.
     * @throws IOException Thrown if an I/O error occurs.
     */
    public static void copy(File src, File dest, boolean overwrite) throws IOException {
        assert src != null;
        assert dest != null;

        /*
         * Supported source & destination:
         * ===============================
         * 1. File -> File
         * 2. File -> Directory
         * 3. Directory -> Directory
         */

        // Source must exist.
        if (!src.exists())
            throw new FileNotFoundException("Source can't be found: " + src);

        // Check that source and destination are not the same.
        if (src.getAbsoluteFile().equals(dest.getAbsoluteFile()))
            throw new IOException("Source and destination are the same [src=" + src + ", dest=" + dest + ']');

        if (dest.exists()) {
            if (!dest.isDirectory() && !overwrite)
                throw new IOException("Destination already exists: " + dest);

            if (!dest.canWrite())
                throw new IOException("Destination is not writable:" + dest);
        }
        else {
            File parent = dest.getParentFile();

            if (parent != null && !parent.exists())
                // Ignore any errors here.
                // We will get errors when we'll try to open the file stream.
                //noinspection ResultOfMethodCallIgnored
                parent.mkdirs();

            // If source is a directory, we should create destination directory.
            if (src.isDirectory())
                //noinspection ResultOfMethodCallIgnored
                dest.mkdir();
        }

        if (src.isDirectory()) {
            // In this case we have Directory -> Directory.
            // Note that we copy the content of the directory and not the directory itself.

            File[] files = src.listFiles();

            for (File file : files) {
                if (file.isDirectory()) {
                    File dir = new File(dest, file.getName());

                    if (!dir.exists() && !dir.mkdirs())
                        throw new IOException("Can't create directory: " + dir);

                    copy(file, dir, overwrite);
                }
                else
                    copy(file, dest, overwrite);
            }
        }
        else {
            // In this case we have File -> File or File -> Directory.
            File file = dest.exists() && dest.isDirectory() ? new File(dest, src.getName()) : dest;

            if (!overwrite && file.exists())
                throw new IOException("Destination already exists: " + file);

            FileInputStream in = null;
            FileOutputStream out = null;

            try {
                in = new FileInputStream(src);
                out = new FileOutputStream(file);

                copy(in, out);
            }
            finally {
                if (in != null)
                    in.close();

                if (out != null) {
                    out.getFD().sync();

                    out.close();
                }
            }
        }
    }

    /**
     * Copies input byte stream to output byte stream.
     *
     * @param in Input byte stream.
     * @param out Output byte stream.
     * @return Number of the copied bytes.
     * @throws IOException Thrown if an I/O error occurs.
     */
    public static int copy(InputStream in, OutputStream out) throws IOException {
        assert in != null;
        assert out != null;

        byte[] buf = new byte[BUF_SIZE];

        int cnt = 0;

        for (int n; (n = in.read(buf)) > 0;) {
            out.write(buf, 0, n);

            cnt += n;
        }

        return cnt;
    }

    /**
     * Copies input character stream to output character stream.
     *
     * @param in Input character stream.
     * @param out Output character stream.
     * @return Number of the copied characters.
     * @throws IOException Thrown if an I/O error occurs.
     */
    public static int copy(Reader in, Writer out) throws IOException {
        assert in != null;
        assert out != null;

        char[] buf = new char[BUF_SIZE];

        int cnt = 0;

        for (int n; (n = in.read(buf)) > 0;) {
            out.write(buf, 0, n);

            cnt += n;
        }

        return cnt;
    }

    /**
     * Writes string to file.
     *
     * @param file File.
     * @param s String to write.
     * @throws IOException Thrown if an I/O error occurs.
     */
    public static void writeStringToFile(File file, String s) throws IOException {
        writeStringToFile(file, s, Charset.defaultCharset().toString(), false);
    }

    /**
     * Writes string to file.
     *
     * @param file File.
     * @param s String to write.
     * @param charset Encoding.
     * @throws IOException Thrown if an I/O error occurs.
     */
    public static void writeStringToFile(File file, String s, String charset) throws IOException {
        writeStringToFile(file, s, charset, false);
    }

    /**
     * Reads file to string using specified charset.
     *
     * @param fileName File name.
     * @param charset File charset.
     * @return File content.
     * @throws IOException If error occurred.
     */
    public static String readFileToString(String fileName, String charset) throws IOException {
        Reader input = new InputStreamReader(new FileInputStream(fileName), charset);

        StringWriter output = new StringWriter();

        char[] buf = new char[4096];

        int n;

        while ((n = input.read(buf)) != -1)
            output.write(buf, 0, n);

        return output.toString();
    }

    /**
     * Writes string to file.
     *
     * @param file File.
     * @param s String to write.
     * @param charset Encoding.
     * @param append If {@code true}, then specified string will be added to the end of the file.
     * @throws IOException Thrown if an I/O error occurs.
     */
    public static void writeStringToFile(File file, String s, String charset, boolean append) throws IOException {
        if (s == null)
            return;

        OutputStream out = null;

        try {
            out = new FileOutputStream(file, append);

            if (s != null)
                out.write(s.getBytes(charset));
        } finally {
            closeQuiet(out);
        }
    }

    /**
     * Utility method that sets cause into exception and returns it.
     *
     * @param e Exception to set cause to and return.
     * @param cause Optional cause to set (if not {@code null}).
     * @param <E> Type of the exception.
     * @return Passed in exception with optionally set cause.
     */
    public static <E extends Throwable> E withCause(E e, @Nullable Throwable cause) {
        assert e != null;

        if (cause != null)
            e.initCause(cause);

        return e;
    }

    /**
     * Deletes file or directory with all sub-directories and files.
     *
     * @param file File or directory to delete.
     * @return {@code true} if and only if the file or directory is successfully deleted,
     *      {@code false} otherwise
     */
    public static boolean delete(File file) {
        assert file != null;

        boolean res = true;

        if (file.isDirectory()) {
            File[] files = file.listFiles();

            if (files != null && files.length > 0)
                for (File file1 : files)
                    if (file1.isDirectory())
                        res &= delete(file1);
                    else if (file1.getName().endsWith("jar"))
                        try {
                            // Why do we do this?
                            new JarFile(file1, false).close();

                            res &= file1.delete();
                        }
                        catch (IOException ignore) {
                            // Ignore it here...
                        }
                    else
                        res &= file1.delete();

            res &= file.delete();
        }
        else
            res = file.delete();

        return res;
    }

    /**
     * @param dir Directory to create along with all non-existent parent directories.
     * @return {@code True} if directory exists (has been created or already existed),
     *      {@code false} if has not been created and does not exist.
     */
    public static boolean mkdirs(File dir) {
        assert dir != null;

        return dir.mkdirs() || dir.exists();
    }

    /**
     * Resolve project home directory based on source code base.
     *
     * @return Project home directory (or {@code null} if it cannot be resolved).
     */
    @Nullable private static String resolveProjectHome() {
        assert Thread.holdsLock(GridUtils.class);

        // Resolve GridGain home via environment variables.
        String ggHome0 = GridSystemProperties.getString(GG_HOME);

        if (!F.isEmpty(ggHome0))
            return ggHome0;

        String appWorkDir = System.getProperty("user.dir");

        if (appWorkDir != null) {
            ggHome0 = findProjectHome(new File(appWorkDir));

            if (ggHome0 != null)
                return ggHome0;
        }

        URI uri;

        Class<GridUtils> cls = GridUtils.class;

        try {
            ProtectionDomain domain = cls.getProtectionDomain();

            // Should not happen, but to make sure our code is not broken.
            if (domain == null || domain.getCodeSource() == null || domain.getCodeSource().getLocation() == null) {
                logResolveFailed(cls, null);

                return null;
            }

            // Resolve path to class-file.
            uri = domain.getCodeSource().getLocation().toURI();

            // Overcome UNC path problem on Windows (http://www.tomergabel.com/JavaMishandlesUNCPathsOnWindows.aspx)
            if (isWindows() && uri.getAuthority() != null)
                uri = new URI(uri.toString().replace("file://", "file:/"));
        }
        catch (URISyntaxException | SecurityException e) {
            logResolveFailed(cls, e);

            return null;
        }

        return findProjectHome(new File(uri));
    }

    /**
     * Tries to find project home starting from specified directory and moving to root.
     *
     * @param startDir First directory in search hierarchy.
     * @return Project home path or {@code null} if it wasn't found.
     */
    private static String findProjectHome(File startDir) {
        for (File cur = startDir.getAbsoluteFile(); cur != null; cur = cur.getParentFile()) {
            // Check 'cur' is project home directory.
            if (!new File(cur, "bin").isDirectory() ||
                !new File(cur, "libs").isDirectory() ||
                !new File(cur, "config").isDirectory())
                continue;

            return cur.getPath();
        }

        return null;
    }

    /**
     * @param cls Class.
     * @param e Exception.
     */
    private static void logResolveFailed(Class cls, Exception e) {
        warn(null, "Failed to resolve GRIDGAIN_HOME automatically for class codebase " +
            "[class=" + cls + (e == null ? "" : ", e=" + e.getMessage()) + ']');
    }

    /**
     * Retrieves {@code GRIDGAIN_HOME} property. The property is retrieved from system
     * properties or from environment in that order.
     *
     * @return {@code GRIDGAIN_HOME} property.
     */
    @Nullable public static String getGridGainHome() {
        GridTuple<String> ggHomeTup = ggHome;

        String ggHome0;

        if (ggHomeTup == null) {
            synchronized (GridUtils.class) {
                // Double check.
                ggHomeTup = ggHome;

                if (ggHomeTup == null) {
                    // Resolve GridGain installation home directory.
                    ggHome = F.t(ggHome0 = resolveProjectHome());

                    if (ggHome0 != null)
                        System.setProperty(GG_HOME, ggHome0);
                }
                else
                    ggHome0 = ggHomeTup.get();
            }
        }
        else
            ggHome0 = ggHomeTup.get();

        return ggHome0;
    }

    /**
     * @param path GridGain home. May be {@code null}.
     */
    public static void setGridGainHome(@Nullable String path) {
        GridTuple<String> ggHomeTup = ggHome;

        String ggHome0;

        if (ggHomeTup == null) {
            synchronized (GridUtils.class) {
                // Double check.
                ggHomeTup = ggHome;

                if (ggHomeTup == null) {
                    if (F.isEmpty(path))
                        System.clearProperty(GG_HOME);
                    else
                        System.setProperty(GG_HOME, path);

                    ggHome = F.t(path);

                    return;
                }
                else
                    ggHome0 = ggHomeTup.get();
            }
        }
        else
            ggHome0 = ggHomeTup.get();

        if (ggHome0 != null && !ggHome0.equals(path))
            throw new GridRuntimeException("Failed to set GRIDGAIN_HOME after it has been already resolved " +
                "[ggHome=" + ggHome0 + ", newGgHome=" + path + ']');
    }

    /**
     * Gets file associated with path.
     * <p>
     * First check if path is relative to {@code GRIDGAIN_HOME}.
     * If not, check if path is absolute.
     * If all checks fail, then {@code null} is returned.
     * <p>
     * See {@link #getGridGainHome()} for information on how {@code GRIDGAIN_HOME} is retrieved.
     *
     * @param path Path to resolve.
     * @return Resolved path as file, or {@code null} if path cannot be resolved.
     */
    @Nullable public static File resolveGridGainPath(String path) {
        assert path != null;

        /*
         * 1. Check relative to GRIDGAIN_HOME specified in configuration, if any.
         */

        String home = getGridGainHome();

        if (home != null) {
            File file = new File(home, path);

            if (file.exists())
                return file;
        }

        /*
         * 2. Check given path as absolute.
         */

        File file = new File(path);

        if (file.exists())
            return file;

        /*
         * 3. Check development path.
         */

        if (home != null)
            file = new File(home, "os/" + path);

        return file.exists() ? file : null;
    }

    /**
     * Gets URL representing the path passed in. First the check is made if path is absolute.
     * If not, then the check is made if path is relative to {@code META-INF} folder in classpath.
     * If not, then the check is made if path is relative to ${GRIDGAIN_HOME}.
     * If all checks fail,
     * then {@code null} is returned, otherwise URL representing path is returned.
     * <p>
     * See {@link #getGridGainHome()} for information on how {@code GRIDGAIN_HOME} is retrieved.
     *
     * @param path Path to resolve.
     * @return Resolved path as URL, or {@code null} if path cannot be resolved.
     * @see #getGridGainHome()
     */
    @Nullable public static URL resolveGridGainUrl(String path) {
        return resolveGridGainUrl(path, true);
    }

    /**
     * Gets URL representing the path passed in. First the check is made if path is absolute.
     * If not, then the check is made if path is relative to {@code META-INF} folder in classpath.
     * If not, then the check is made if path is relative to ${GRIDGAIN_HOME}.
     * If all checks fail,
     * then {@code null} is returned, otherwise URL representing path is returned.
     * <p>
     * See {@link #getGridGainHome()} for information on how {@code GRIDGAIN_HOME} is retrieved.
     *
     * @param path Path to resolve.
     * @param metaInf Flag to indicate whether META-INF folder should be checked or class path root.
     * @return Resolved path as URL, or {@code null} if path cannot be resolved.
     * @see #getGridGainHome()
     */
    @SuppressWarnings({"UnusedCatchParameter"})
    @Nullable public static URL resolveGridGainUrl(String path, boolean metaInf) {
        File f = resolveGridGainPath(path);

        if (f == null)
            f = resolveGridGainPath("os/" + path);

        if (f != null) {
            try {
                // Note: we use that method's chain instead of File.getURL() with due
                // Sun bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6179468
                return f.toURI().toURL();
            }
            catch (MalformedURLException e) {
                // No-op.
            }
        }

        String locPath = (metaInf ? "META-INF/" : "") + path.replaceAll("\\\\", "/");

        return Thread.currentThread().getContextClassLoader().getResource(locPath);
    }

    /**
     * Join byte arrays into single one.
     *
     * @param bufs list of byte arrays to concatenate.
     * @return Concatenated byte's array.
     */
    public static byte[] join(byte[]... bufs) {
        int size = 0;
        for (byte[] buf : bufs) {
            size += buf.length;
        }

        byte[] res = new byte[size];
        int position = 0;
        for (byte[] buf : bufs) {
            arrayCopy(buf, 0, res, position, buf.length);
            position += buf.length;
        }

        return res;
    }

    /**
     * Converts byte array to formatted string. If calling:
     * <pre name="code" class="java">
     * ...
     * byte[] data = {10, 20, 30, 40, 50, 60, 70, 80, 90};
     *
     * U.byteArray2String(data, "0x%02X", ",0x%02X")
     * ...
     * </pre>
     * the result will be:
     * <pre name="code" class="java">
     * ...
     * 0x0A, 0x14, 0x1E, 0x28, 0x32, 0x3C, 0x46, 0x50, 0x5A
     * ...
     * </pre>
     *
     * @param arr Array of byte.
     * @param hdrFmt C-style string format for the first element.
     * @param bodyFmt C-style string format for second and following elements, if any.
     * @return String with converted bytes.
     */
    public static String byteArray2String(byte[] arr, String hdrFmt, String bodyFmt) {
        assert arr != null;
        assert hdrFmt != null;
        assert bodyFmt != null;

        SB sb = new SB();

        sb.a('{');

        boolean first = true;

        for (byte b : arr)
            if (first) {
                sb.a(String.format(hdrFmt, b));

                first = false;
            }
            else
                sb.a(String.format(bodyFmt, b));

        sb.a('}');

        return sb.toString();
    }

    /**
     * Converts byte array to hex string.
     *
     * @param arr Array of bytes.
     * @return Hex string.
     */
    public static String byteArray2HexString(byte[] arr) {
        SB sb = new SB(arr.length << 1);

        for (byte b : arr)
            sb.a(Integer.toHexString(MASK & b >>> 4)).a(Integer.toHexString(MASK & b));

        return sb.toString().toUpperCase();
    }

    /**
     * Convert string with hex values to byte array.
     *
     * @param hex Hexadecimal string to convert.
     * @return array of bytes defined as hex in string.
     * @throws IllegalArgumentException If input character differs from certain hex characters.
     */
    public static byte[] hexString2ByteArray(String hex) throws IllegalArgumentException {
        // If Hex string has odd character length.
        if (hex.length() % 2 != 0)
            hex = '0' + hex;

        char[] chars = hex.toCharArray();

        byte[] bytes = new byte[chars.length / 2];

        int byteCnt = 0;

        for (int i = 0; i < chars.length; i += 2) {
            int newByte = 0;

            newByte |= hexCharToByte(chars[i]);

            newByte <<= 4;

            newByte |= hexCharToByte(chars[i + 1]);

            bytes[byteCnt] = (byte)newByte;

            byteCnt++;
        }

        return bytes;
    }

    /**
     * Return byte value for certain character.
     *
     * @param ch Character
     * @return Byte value.
     * @throws IllegalArgumentException If input character differ from certain hex characters.
     */
    @SuppressWarnings({"UnnecessaryFullyQualifiedName", "fallthrough"})
    private static byte hexCharToByte(char ch) throws IllegalArgumentException {
        switch (ch) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return (byte)(ch - '0');

            case 'a':
            case 'A':
                return 0xa;

            case 'b':
            case 'B':
                return 0xb;

            case 'c':
            case 'C':
                return 0xc;

            case 'd':
            case 'D':
                return 0xd;

            case 'e':
            case 'E':
                return 0xe;

            case 'f':
            case 'F':
                return 0xf;

            default:
                throw new IllegalArgumentException("Hex decoding wrong input character [character=" + ch + ']');
        }
    }

    /**
     * Converts primitive double to byte array.
     *
     * @param d Double to convert.
     * @return Byte array.
     */
    public static byte[] doubleToBytes(double d) {
        return longToBytes(Double.doubleToLongBits(d));
    }

    /**
     * Converts primitive {@code double} type to byte array and stores
     * it in the specified byte array.
     *
     * @param d Double to convert.
     * @param bytes Array of bytes.
     * @param off Offset.
     * @return New offset.
     */
    public static int doubleToBytes(double d, byte[] bytes, int off) {
        return longToBytes(Double.doubleToLongBits(d), bytes, off);
    }

    /**
     * Converts primitive float to byte array.
     *
     * @param f Float to convert.
     * @return Array of bytes.
     */
    public static byte[] floatToBytes(float f) {
        return intToBytes(Float.floatToIntBits(f));
    }

    /**
     * Converts primitive float to byte array.
     *
     * @param f Float to convert.
     * @param bytes Array of bytes.
     * @param off Offset.
     * @return New offset.
     */
    public static int floatToBytes(float f, byte[] bytes, int off) {
        return intToBytes(Float.floatToIntBits(f), bytes, off);
    }

    /**
     * Converts primitive {@code long} type to byte array.
     *
     * @param l Long value.
     * @return Array of bytes.
     */
    public static byte[] longToBytes(long l) {
        return GridClientByteUtils.longToBytes(l);
    }

    /**
     * Converts primitive {@code long} type to byte array and stores it in specified
     * byte array.
     *
     * @param l Long value.
     * @param bytes Array of bytes.
     * @param off Offset in {@code bytes} array.
     * @return Number of bytes overwritten in {@code bytes} array.
     */
    public static int longToBytes(long l, byte[] bytes, int off) {
        return off + GridClientByteUtils.longToBytes(l, bytes, off);
    }

    /**
     * Converts primitive {@code int} type to byte array.
     *
     * @param i Integer value.
     * @return Array of bytes.
     */
    public static byte[] intToBytes(int i) {
        return GridClientByteUtils.intToBytes(i);
    }

    /**
     * Converts primitive {@code int} type to byte array and stores it in specified
     * byte array.
     *
     * @param i Integer value.
     * @param bytes Array of bytes.
     * @param off Offset in {@code bytes} array.
     * @return Number of bytes overwritten in {@code bytes} array.
     */
    public static int intToBytes(int i, byte[] bytes, int off) {
        return off + GridClientByteUtils.intToBytes(i, bytes, off);
    }

    /**
     * Converts primitive {@code short} type to byte array.
     *
     * @param s Short value.
     * @return Array of bytes.
     */
    public static byte[] shortToBytes(short s) {
        return GridClientByteUtils.shortToBytes(s);
    }

    /**
     * Converts primitive {@code short} type to byte array and stores it in specified
     * byte array.
     *
     * @param s Short value.
     * @param bytes Array of bytes.
     * @param off Offset in {@code bytes} array.
     * @return Number of bytes overwritten in {@code bytes} array.
     */
    public static int shortToBytes(short s, byte[] bytes, int off) {
        return off + GridClientByteUtils.shortToBytes(s, bytes, off);
    }

    /**
     * Encodes {@link java.util.UUID} into a sequence of bytes using the {@link java.nio.ByteBuffer},
     * storing the result into a new byte array.
     *
     * @param uuid Unique identifier.
     * @param arr Byte array to fill with result.
     * @param off Offset in {@code arr}.
     * @return Number of bytes overwritten in {@code bytes} array.
     */
    public static int uuidToBytes(@Nullable UUID uuid, byte[] arr, int off) {
        return off + GridClientByteUtils.uuidToBytes(uuid, arr, off);
    }

    /**
     * Converts an UUID to byte array.
     *
     * @param uuid UUID value.
     * @return Encoded into byte array {@link java.util.UUID}.
     */
    public static byte[] uuidToBytes(@Nullable UUID uuid) {
        return GridClientByteUtils.uuidToBytes(uuid);
    }

    /**
     * Constructs {@code short} from byte array.
     *
     * @param bytes Array of bytes.
     * @param off Offset in {@code bytes} array.
     * @return Short value.
     */
    public static short bytesToShort(byte[] bytes, int off) {
        assert bytes != null;

        int bytesCnt = Short.SIZE >> 3;

        if (off + bytesCnt > bytes.length)
            // Just use the remainder.
            bytesCnt = bytes.length - off;

        short res = 0;

        for (int i = 0; i < bytesCnt; i++) {
            int shift = bytesCnt - i - 1 << 3;

            res |= (0xffL & bytes[off++]) << shift;
        }

        return res;
    }

    /**
     * Constructs {@code int} from byte array.
     *
     * @param bytes Array of bytes.
     * @param off Offset in {@code bytes} array.
     * @return Integer value.
     */
    public static int bytesToInt(byte[] bytes, int off) {
        assert bytes != null;

        int bytesCnt = Integer.SIZE >> 3;

        if (off + bytesCnt > bytes.length)
            // Just use the remainder.
            bytesCnt = bytes.length - off;

        int res = 0;

        for (int i = 0; i < bytesCnt; i++) {
            int shift = bytesCnt - i - 1 << 3;

            res |= (0xffL & bytes[off++]) << shift;
        }

        return res;
    }

    /**
     * Constructs {@code long} from byte array.
     *
     * @param bytes Array of bytes.
     * @param off Offset in {@code bytes} array.
     * @return Long value.
     */
    public static long bytesToLong(byte[] bytes, int off) {
        assert bytes != null;

        int bytesCnt = Long.SIZE >> 3;

        if (off + bytesCnt > bytes.length)
            bytesCnt = bytes.length - off;

        long res = 0;

        for (int i = 0; i < bytesCnt; i++) {
            int shift = bytesCnt - i - 1 << 3;

            res |= (0xffL & bytes[off++]) << shift;
        }

        return res;
    }

    /**
     * Reads an {@link UUID} form byte array.
     * If given array contains all 0s then {@code null} will be returned.
     *
     * @param bytes array of bytes.
     * @param off Offset in {@code bytes} array.
     * @return UUID value or {@code null}.
     */
    public static UUID bytesToUuid(byte[] bytes, int off) {
        return GridClientByteUtils.bytesToUuid(bytes, off);
    }

    /**
     * Constructs double from byte array.
     *
     * @param bytes Byte array.
     * @param off Offset in {@code bytes} array.
     * @return Double value.
     */
    public static double bytesToDouble(byte[] bytes, int off) {
        return Double.longBitsToDouble(bytesToLong(bytes, off));
    }

    /**
     * Constructs float from byte array.
     *
     * @param bytes Byte array.
     * @param off Offset in {@code bytes} array.
     * @return Float value.
     */
    public static float bytesToFloat(byte[] bytes, int off) {
        return Float.intBitsToFloat(bytesToInt(bytes, off));
    }

    /**
     * Compares fragments of byte arrays.
     *
     * @param a First array.
     * @param aOff First array offset.
     * @param b Second array.
     * @param bOff Second array offset.
     * @param len Length of fragments.
     * @return {@code true} if fragments are equal, {@code false} otherwise.
     */
    public static boolean bytesEqual(byte[] a, int aOff, byte[] b, int bOff, int len) {
        if (aOff + len > a.length || bOff + len > b.length)
            return false;
        else {
            for (int i = 0; i < len; i++)
                if (a[aOff + i] != b[bOff + i])
                    return false;

            return true;
        }
    }

    /**
     * Checks for containment of the value in the array.
     * Both array cells and value may be {@code null}. Two {@code null}s are considered equal.
     *
     * @param arr Array of objects.
     * @param val Value to check for containment inside of array.
     * @param vals Additional values.
     * @return {@code true} if contains object, {@code false} otherwise.
     */
    public static boolean containsObjectArray(@Nullable Object[] arr, Object val, @Nullable Object... vals) {
        if (arr == null || arr.length == 0)
            return false;

        for (Object o : arr) {
            if (F.eq(o, val))
                return true;

            if (vals != null && vals.length > 0)
                for (Object v : vals)
                    if (F.eq(o, v))
                        return true;
        }

        return false;
    }

    /**
     * Checks for containment of the value in the array.
     * Both array cells and value may be {@code null}. Two {@code null}s are considered equal.
     *
     * @param arr Array of objects.
     * @param c Collection to check.
     * @return {@code true} if contains object, {@code false} otherwise.
     */
    public static boolean containsObjectArray(@Nullable Object[] arr, @Nullable Collection<Object> c) {
        if (arr == null || arr.length == 0 || c == null || c.isEmpty())
            return false;

        for (Object o : arr) {
            if (c.contains(o))
                return true;
        }

        return false;
    }

    /**
     * Checks for containment of the value in the array.
     *
     * @param arr Array of objects.
     * @param val Value to check for containment inside of array.
     * @return {@code true} if contains object, {@code false} otherwise.
     */
    public static boolean containsIntArray(int[] arr, int val) {
        assert arr != null;

        if (arr.length == 0)
            return false;

        for (int i : arr)
            if (i == val)
                return true;

        return false;
    }

    /**
     * Checks for containment of given string value in the specified array.
     * Array's cells and string value can be {@code null}. Tow {@code null}s are considered equal.
     *
     * @param arr Array of strings.
     * @param val Value to check for containment inside of array.
     * @param ignoreCase Ignoring case if {@code true}.
     * @return {@code true} if contains string, {@code false} otherwise.
     */
    public static boolean containsStringArray(String[] arr, @Nullable String val, boolean ignoreCase) {
        assert arr != null;

        for (String s : arr) {
            // If both are nulls, then they are equal.
            if (s == null && val == null)
                return true;

            // Only one is null and the other one isn't.
            if (s == null || val == null)
                continue;

            // Both are not nulls.
            if (ignoreCase) {
                if (s.equalsIgnoreCase(val))
                    return true;
            }
            else if (s.equals(val))
                return true;
        }

        return false;
    }

    /**
     * Checks for containment of given string value in the specified collection.
     * Collection elements and string value can be {@code null}. Tow {@code null}s are considered equal.
     *
     * @param c Array of strings.
     * @param val Value to check for containment inside of array.
     * @param ignoreCase Ignoring case if {@code true}.
     * @return {@code true} if contains string, {@code false} otherwise.
     */
    public static boolean containsStringCollection(Iterable<String> c, @Nullable String val, boolean ignoreCase) {
        assert c != null;

        for (String s : c) {
            // If both are nulls, then they are equal.
            if (s == null && val == null)
                return true;

            // Only one is null and the other one isn't.
            if (s == null || val == null)
                continue;

            // Both are not nulls.
            if (ignoreCase) {
                if (s.equalsIgnoreCase(val))
                    return true;
            }
            else if (s.equals(val))
                return true;
        }

        return false;
    }

    /**
     * Checks for containment of value matching given regular expression in the provided array.
     *
     * @param arr Array of strings.
     * @param regex Regular expression.
     * @return {@code true} if string matching given regular expression found, {@code false} otherwise.
     */
    public static boolean containsRegexArray(String[] arr, String regex) {
        assert arr != null;
        assert regex != null;

        for (String s : arr)
            if (s != null && s.matches(regex))
                return true;

        return false;
    }

    /**
     * Closes given resource logging possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     * @param log Logger to log possible checked exception with (optional).
     */
    public static void close(@Nullable Closeable rsrc, @Nullable GridLogger log) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (IOException e) {
                warn(log, "Failed to close resource: " + e.getMessage());
            }
    }

    /**
     * Quietly closes given resource ignoring possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     */
    public static void closeQuiet(@Nullable AutoCloseable rsrc) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (Exception ignored) {
                // No-op.
            }
    }

    /**
     * Quietly closes given resource ignoring possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     */
    public static void closeQuiet(@Nullable Closeable rsrc) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (IOException ignored) {
                // No-op.
            }
    }

    /**
     * Closes given resource logging possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     * @param log Logger to log possible checked exception with (optional).
     */
    public static void close(@Nullable Socket rsrc, @Nullable GridLogger log) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (IOException e) {
                warn(log, "Failed to close resource: " + e.getMessage());
            }
    }

    /**
     * Quietly closes given resource ignoring possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     */
    public static void closeQuiet(@Nullable Socket rsrc) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (IOException ignored) {
                // No-op.
            }
    }

    /**
     * Closes given resource logging possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     * @param log Logger to log possible checked exception with (optional).
     */
    public static void close(@Nullable ServerSocket rsrc, @Nullable GridLogger log) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (IOException e) {
                warn(log, "Failed to close resource: " + e.getMessage());
            }
    }

    /**
     * Quietly closes given resource ignoring possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     */
    public static void closeQuiet(@Nullable ServerSocket rsrc) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (IOException ignored) {
                // No-op.
            }
    }

    /**
     * Closes given resource logging possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     * @param log Logger to log possible checked exception with (optional).
     */
    public static void close(@Nullable AbstractInterruptibleChannel rsrc, @Nullable GridLogger log) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (IOException e) {
                warn(log, "Failed to close resource: " + e.getMessage());
            }
    }

    /**
     * Quietly closes given resource ignoring possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     */
    public static void closeQuiet(@Nullable AbstractInterruptibleChannel rsrc) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (IOException ignored) {
                // No-op.
            }
    }

    /**
     * Closes given resource logging possible checked exceptions.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     * @param log Logger to log possible checked exception with (optional).
     */
    public static void close(@Nullable SelectionKey rsrc, @Nullable GridLogger log) {
        if (rsrc != null)
            // This apply will automatically deregister the selection key as well.
            close(rsrc.channel(), log);
    }

    /**
     * Quietly closes given resource ignoring possible checked exceptions.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     */
    public static void closeQuiet(@Nullable SelectionKey rsrc) {
        if (rsrc != null)
            // This apply will automatically deregister the selection key as well.
            closeQuiet(rsrc.channel());
    }

    /**
     * Closes given resource logging possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     * @param log Logger to log possible checked exception with (optional).
     */
    public static void close(@Nullable Reader rsrc, @Nullable GridLogger log) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (IOException e) {
                warn(log, "Failed to close resource: " + e.getMessage());
            }
    }

    /**
     * Quietly closes given resource ignoring possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     */
    public static void closeQuiet(@Nullable Reader rsrc) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (IOException ignored) {
                // No-op.
            }
    }

    /**
     * Quietly closes given resource ignoring possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     */
    public static void closeQuiet(@Nullable Writer rsrc) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (IOException ignored) {
                // No-op.
            }
    }

    /**
     * Closes given resource logging possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     * @param log Logger to log possible checked exception with (optional).
     */
    public static void close(@Nullable ZipFile rsrc, @Nullable GridLogger log) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (IOException e) {
                warn(log, "Failed to close resource: " + e.getMessage());
            }
    }

    /**
     * Quietly closes given resource ignoring possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     */
    public static void closeQuiet(@Nullable ZipFile rsrc) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (IOException ignored) {
                // No-op.
            }
    }

    /**
     * Closes given resource.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     */
    public static void close(@Nullable DatagramSocket rsrc) {
        if (rsrc != null)
            rsrc.close();
    }

    /**
     * Closes given resource logging possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     * @param log Logger to log possible checked exception with (optional).
     */
    public static void close(@Nullable Selector rsrc, @Nullable GridLogger log) {
        if (rsrc != null)
            try {
                if (rsrc.isOpen())
                    rsrc.close();
            }
            catch (IOException e) {
                warn(log, "Failed to close resource: " + e.getMessage());
            }
    }

    /**
     * Quietly closes given resource ignoring possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     */
    public static void closeQuiet(@Nullable Selector rsrc) {
        if (rsrc != null)
            try {
                if (rsrc.isOpen())
                    rsrc.close();
            }
            catch (IOException ignored) {
                // No-op.
            }
    }

    /**
     * Closes given resource logging possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     * @param log Logger to log possible checked exception with (optional).
     */
    public static void close(@Nullable Context rsrc, @Nullable GridLogger log) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (NamingException e) {
                warn(log, "Failed to close resource: " + e.getMessage());
            }
    }

    /**
     * Quietly closes given resource ignoring possible checked exception.
     *
     * @param rsrc Resource to close. If it's {@code null} - it's no-op.
     */
    public static void closeQuiet(@Nullable Context rsrc) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (NamingException ignored) {
                // No-op.
            }
    }

    /**
     * Closes JDBC connection logging possible checked exception.
     *
     * @param rsrc JDBC connection to close. If connection is {@code null}, it's no-op.
     * @param log Logger to log possible checked exception with (optional).
     */
    public static void close(@Nullable Connection rsrc, @Nullable GridLogger log) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (SQLException e) {
                warn(log, "Failed to close resource: " + e.getMessage());
            }
    }

    /**
     * Quietly closes JDBC connection ignoring possible checked exception.
     *
     * @param rsrc JDBC connection to close. If connection is {@code null}, it's no-op.
     */
    public static void closeQuiet(@Nullable Connection rsrc) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (SQLException ignored) {
                // No-op.
            }
    }

    /**
     * Closes JDBC statement logging possible checked exception.
     *
     * @param rsrc JDBC statement to close. If statement is {@code null}, it's no-op.
     * @param log Logger to log possible checked exception with (optional).
     */
    public static void close(@Nullable Statement rsrc, @Nullable GridLogger log) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (SQLException e) {
                warn(log, "Failed to close resource: " + e.getMessage());
            }
    }

    /**
     * Quietly closes JDBC statement ignoring possible checked exception.
     *
     * @param rsrc JDBC statement to close. If statement is {@code null}, it's no-op.
     */
    public static void closeQuiet(@Nullable Statement rsrc) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (SQLException ignored) {
                // No-op.
            }
    }

    /**
     * Closes JDBC result set logging possible checked exception.
     *
     * @param rsrc JDBC result set to close. If result set is {@code null}, it's no-op.
     * @param log Logger to log possible checked exception with (optional).
     */
    public static void close(@Nullable ResultSet rsrc, @Nullable GridLogger log) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (SQLException e) {
                warn(log, "Failed to close resource: " + e.getMessage());
            }
    }

    /**
     * Quietly closes JDBC result set ignoring possible checked exception.
     *
     * @param rsrc JDBC result set to close. If result set is {@code null}, it's no-op.
     */
    public static void closeQuiet(@Nullable ResultSet rsrc) {
        if (rsrc != null)
            try {
                rsrc.close();
            }
            catch (SQLException ignored) {
                // No-op.
            }
    }

    /**
     * Closes class loader logging possible checked exception.
     * Note: this issue for problem <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5041014">
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5041014</a>.
     *
     * @param clsLdr Class loader. If it's {@code null} - it's no-op.
     * @param log Logger to log possible checked exception with (optional).
     */
    public static void close(@Nullable URLClassLoader clsLdr, @Nullable GridLogger log) {
        if (clsLdr != null)
            try {
                URLClassPath path = SharedSecrets.getJavaNetAccess().getURLClassPath(clsLdr);

                Field ldrFld = path.getClass().getDeclaredField("loaders");

                ldrFld.setAccessible(true);

                Iterable ldrs = (Iterable)ldrFld.get(path);

                for (Object ldr : ldrs)
                    if (ldr.getClass().getName().endsWith("JarLoader"))
                        try {
                            Field jarFld = ldr.getClass().getDeclaredField("jar");

                            jarFld.setAccessible(true);

                            ZipFile jar = (ZipFile)jarFld.get(ldr);

                            jar.close();
                        }
                        catch (Exception e) {
                            warn(log, "Failed to close resource: " + e.getMessage());
                        }
            }
            catch (Exception e) {
                warn(log, "Failed to close resource: " + e.getMessage());
            }
    }

    /**
     * Quietly releases file lock ignoring all possible exceptions.
     *
     * @param lock File lock. If it's {@code null} - it's no-op.
     */
    public static void releaseQuiet(@Nullable FileLock lock) {
        if (lock != null)
            try {
                lock.release();
            }
            catch (Exception ignored) {
                // No-op.
            }
    }

    /**
     * Rollbacks JDBC connection logging possible checked exception.
     *
     * @param rsrc JDBC connection to rollback. If connection is {@code null}, it's no-op.
     * @param log Logger to log possible checked exception with (optional).
     */
    public static void rollbackConnection(@Nullable Connection rsrc, @Nullable GridLogger log) {
        if (rsrc != null)
            try {
                rsrc.rollback();
            }
            catch (SQLException e) {
                warn(log, "Failed to rollback JDBC connection: " + e.getMessage());
            }
    }

    /**
     * Depending on whether or not log is provided and quiet mode is enabled logs given messages as
     * quiet message or normal log WARN message in {@code org.gridgain.grid.CourtesyConfigNotice}
     * category. If {@code log} is {@code null} or in QUIET mode it will add {@code (courtesy)}
     * prefix to the message.
     *
     * @param log Optional logger to use when QUIET mode is not enabled.
     * @param msg Message to log.
     */
    public static void courtesy(@Nullable GridLogger log, Object msg) {
        assert msg != null;

        String s = msg.toString();

        courtesy(log, s, s);
    }

    /**
     * Depending on whether or not log is provided and quiet mode is enabled logs given messages as
     * quiet message or normal log WARN message in {@code org.gridgain.grid.CourtesyConfigNotice}
     * category. If {@code log} is {@code null} or in QUIET mode it will add {@code (courtesy)}
     * prefix to the message.
     *
     * @param log Optional logger to use when QUIET mode is not enabled.
     * @param longMsg Message to log using normal logger.
     * @param shortMsg Message to log using quiet logger.
     */
    public static void courtesy(@Nullable GridLogger log, Object longMsg, Object shortMsg) {
        assert longMsg != null;
        assert shortMsg != null;

        if (log != null)
            log.getLogger(GridConfiguration.COURTESY_LOGGER_NAME).warning(compact(longMsg.toString()));
        else
            X.println("[" + SHORT_DATE_FMT.format(new java.util.Date()) + "] (courtesy) " +
                compact(shortMsg.toString()));
    }

    /**
     * Depending on whether or not log is provided and quiet mode is enabled logs given
     * messages as quiet message or normal log WARN message. If {@code log} is {@code null}
     * or in QUIET mode it will add {@code (wrn)} prefix to the message.
     *
     * @param log Optional logger to use when QUIET mode is not enabled.
     * @param msg Message to log.
     */
    public static void warn(@Nullable GridLogger log, Object msg) {
        assert msg != null;

        String s = msg.toString();

        warn(log, s, s);
    }

    /**
     * Logs warning message in both verbose and quite modes.
     *
     * @param log Logger to use.
     * @param msg Message to log.
     */
    public static void quietAndWarn(GridLogger log, Object msg) {
        quietAndWarn(log, msg, msg);
    }

    /**
     * Logs warning message in both verbose and quite modes.
     *
     * @param log Logger to use.
     * @param shortMsg Short message.
     * @param msg Message to log.
     */
    public static void quietAndWarn(GridLogger log, Object msg, Object shortMsg) {
        warn(log, msg);

        if (log.isQuiet())
            quiet(false, shortMsg);
    }

    /**
     * Depending on whether or not log is provided and quiet mode is enabled logs given
     * messages as quiet message or normal log ERROR message. If {@code log} is {@code null}
     * or in QUIET mode it will add {@code (err)} prefix to the message.
     *
     * @param log Optional logger to use when QUIET mode is not enabled.
     * @param msg Message to log.
     */
    public static void error(@Nullable GridLogger log, Object msg) {
        assert msg != null;

        if (msg instanceof Throwable) {
            Throwable t = (Throwable)msg;

            error(log, t.getMessage(), t);
        }
        else {
            String s = msg.toString();

            error(log, s, s, null);
        }
    }

    /**
     * Depending on whether or not log is provided and quiet mode is enabled logs given
     * messages as quiet message or normal log WARN message. If {@code log} is {@code null}
     * or in QUIET mode it will add {@code (wrn)} prefix to the message.
     *
     * @param log Optional logger to use when QUIET mode is not enabled.
     * @param longMsg Message to log using normal logger.
     * @param shortMsg Message to log using quiet logger.
     */
    public static void warn(@Nullable GridLogger log, Object longMsg, Object shortMsg) {
        assert longMsg != null;
        assert shortMsg != null;

        if (log != null)
            log.warning(compact(longMsg.toString()));
        else
            X.println("[" + SHORT_DATE_FMT.format(new java.util.Date()) + "] (wrn) " +
                compact(shortMsg.toString()));
    }

    /**
     * Depending on whether or not log is provided and quiet mode is enabled logs given
     * messages as quiet message or normal log INFO message.
     * <p>
     * <b>NOTE:</b> unlike the normal logging when INFO level may not be enabled and
     * therefore no logging will happen - using this method the log will be written
     * always either via INFO log or quiet mode.
     * <p>
     * <b>USE IT APPROPRIATELY.</b>
     *
     * @param log Optional logger to use when QUIET mode is not enabled.
     * @param longMsg Message to log using normal logger.
     * @param shortMsg Message to log using quiet logger.
     */
    public static void log(@Nullable GridLogger log, Object longMsg, Object shortMsg) {
        assert longMsg != null;
        assert shortMsg != null;

        if (log != null) {
            if (log.isInfoEnabled())
                log.info(compact(longMsg.toString()));
        }
        else
            quiet(false, shortMsg);
    }

    /**
     * Depending on whether or not log is provided and quiet mode is enabled logs given
     * messages as quiet message or normal log INF0 message.
     * <p>
     * <b>NOTE:</b> unlike the normal logging when INFO level may not be enabled and
     * therefore no logging will happen - using this method the log will be written
     * always either via INFO log or quiet mode.
     * <p>
     * <b>USE IT APPROPRIATELY.</b>
     *
     * @param log Optional logger to use when QUIET mode is not enabled.
     * @param msg Message to log.
     */
    public static void log(@Nullable GridLogger log, Object msg) {
        assert msg != null;

        String s = msg.toString();

        log(log, s, s);
    }

    /**
     * Depending on whether or not log is provided and quiet mode is enabled logs given
     * messages as quiet message or normal log ERROR message. If {@code log} is {@code null}
     * or in QUIET mode it will add {@code (err)} prefix to the message.
     *
     * @param log Optional logger to use when QUIET mode is not enabled.
     * @param longMsg Message to log using normal logger.
     * @param shortMsg Message to log using quiet logger.
     * @param e Optional exception.
     */
    public static void error(@Nullable GridLogger log, Object longMsg, Object shortMsg, @Nullable Throwable e) {
        assert longMsg != null;
        assert shortMsg != null;

        if (log != null) {
            if (e == null)
                log.error(compact(longMsg.toString()));
            else
                log.error(compact(longMsg.toString()), e);
        }
        else {
            X.printerr("[" + SHORT_DATE_FMT.format(new java.util.Date()) + "] (err) " +
                compact(shortMsg.toString()));

            if (e != null)
                e.printStackTrace(System.err);
            else
                X.printerrln();
        }
    }

    /**
     * Shortcut for {@link #error(GridLogger, Object, Object, Throwable)}.
     *
     * @param log Optional logger.
     * @param shortMsg Message to log using quiet logger.
     * @param e Optional exception.
     */
    public static void error(@Nullable GridLogger log, Object shortMsg, @Nullable Throwable e) {
        assert shortMsg != null;

        String s = shortMsg.toString();

        error(log, s, s, e);
    }

    /**
     *
     * @param err Whether to print to {@code System.err}.
     * @param objs Objects to log in quiet mode.
     */
    public static void quiet(boolean err, Object... objs) {
        assert objs != null;

        String time = SHORT_DATE_FMT.format(new java.util.Date());

        SB sb = new SB();

        for (Object obj : objs)
            sb.a('[').a(time).a("] ").a(obj.toString()).a(NL);

        PrintStream ps = err ? System.err : System.out;

        ps.print(compact(sb.toString()));
    }

    /**
     * Prints out the message in quite and info modes.
     *
     * @param log Logger.
     * @param msg Message to print.
     */
    public static void quietAndInfo(GridLogger log, String msg) {
        if (log.isQuiet())
            U.quiet(false, msg);

        if (log.isInfoEnabled())
            log.info(msg);
    }

    /**
     * Quietly rollbacks JDBC connection ignoring possible checked exception.
     *
     * @param rsrc JDBC connection to rollback. If connection is {@code null}, it's no-op.
     */
    public static void rollbackConnectionQuiet(@Nullable Connection rsrc) {
        if (rsrc != null)
            try {
                rsrc.rollback();
            }
            catch (SQLException ignored) {
                // No-op.
            }
    }

    /**
     * Constructs JMX object name with given properties.
     * Map with ordered {@code groups} used for proper object name construction.
     *
     * @param gridName Grid name.
     * @param grp Name of the group.
     * @param name Name of mbean.
     * @return JMX object name.
     * @throws MalformedObjectNameException Thrown in case of any errors.
     */
    public static ObjectName makeMBeanName(@Nullable String gridName, @Nullable String grp, String name)
        throws MalformedObjectNameException {
        SB sb = new SB(JMX_DOMAIN + ':');

        if (gridName != null && !gridName.isEmpty())
            sb.a("grid=").a(gridName).a(',');

        if (grp != null)
            sb.a("group=").a(grp).a(',');

        sb.a("name=").a(name);

        return new ObjectName(sb.toString());
    }

    /**
     * Mask component name to make sure that it is not {@code null}.
     *
     * @param cacheName Component name to mask, possibly {@code null}.
     * @return Component name.
     */
    public static String maskName(@Nullable String cacheName) {
        return cacheName == null ? "default" : cacheName;
    }

    /**
     * Constructs JMX object name with given properties.
     * Map with ordered {@code groups} used for proper object name construction.
     *
     * @param gridName Grid name.
     * @param cacheName Name of the cache.
     * @param name Name of mbean.
     * @return JMX object name.
     * @throws MalformedObjectNameException Thrown in case of any errors.
     */
    public static ObjectName makeCacheMBeanName(@Nullable String gridName, @Nullable String cacheName, String name)
        throws MalformedObjectNameException {
        SB sb = new SB(JMX_DOMAIN + ':');

        if (gridName != null && !gridName.isEmpty())
            sb.a("grid=").a(gridName).a(',');

        cacheName = maskName(cacheName);

        sb.a("group=").a(cacheName).a(',');

        sb.a("name=").a(name);

        return new ObjectName(sb.toString());
    }

    /**
     * Registers MBean with the server.
     *
     * @param <T> Type of mbean.
     * @param mbeanSrv MBean server.
     * @param gridName Grid name.
     * @param grp Name of the group.
     * @param name Name of mbean.
     * @param impl MBean implementation.
     * @param itf MBean interface.
     * @return JMX object name.
     * @throws JMException If MBean creation failed.
     */
    public static <T> ObjectName registerMBean(MBeanServer mbeanSrv, @Nullable String gridName, @Nullable String grp,
        String name, T impl, @Nullable Class<T> itf) throws JMException {
        assert mbeanSrv != null;
        assert name != null;
        assert itf != null;

        DynamicMBean mbean = new GridStandardMBean(impl, itf);

        mbean.getMBeanInfo();

        return mbeanSrv.registerMBean(mbean, makeMBeanName(gridName, grp, name)).getObjectName();
    }

    /**
     * Registers MBean with the server.
     *
     * @param <T> Type of mbean.
     * @param mbeanSrv MBean server.
     * @param name MBean object name.
     * @param impl MBean implementation.
     * @param itf MBean interface.
     * @return JMX object name.
     * @throws JMException If MBean creation failed.
     */
    public static <T> ObjectName registerMBean(MBeanServer mbeanSrv, ObjectName name, T impl, Class<T> itf)
        throws JMException {
        assert mbeanSrv != null;
        assert name != null;
        assert itf != null;

        DynamicMBean mbean = new GridStandardMBean(impl, itf);

        mbean.getMBeanInfo();

        return mbeanSrv.registerMBean(mbean, name).getObjectName();
    }

    /**
     * Registers MBean with the server.
     *
     * @param <T> Type of mbean.
     * @param mbeanSrv MBean server.
     * @param gridName Grid name.
     * @param cacheName Name of the cache.
     * @param name Name of mbean.
     * @param impl MBean implementation.
     * @param itf MBean interface.
     * @return JMX object name.
     * @throws JMException If MBean creation failed.
     */
    public static <T> ObjectName registerCacheMBean(MBeanServer mbeanSrv, @Nullable String gridName,
        @Nullable String cacheName, String name, T impl, Class<T> itf) throws JMException {
        assert mbeanSrv != null;
        assert name != null;
        assert itf != null;

        DynamicMBean mbean = new GridStandardMBean(impl, itf);

        mbean.getMBeanInfo();

        return mbeanSrv.registerMBean(mbean, makeCacheMBeanName(gridName, cacheName, name)).getObjectName();
    }

    /**
     * Convenience method that interrupts a given thread if it's not {@code null}.
     *
     * @param t Thread to interrupt.
     */
    public static void interrupt(@Nullable Thread t) {
        if (t != null)
            t.interrupt();
    }

    /**
     * Convenience method that interrupts a given thread if it's not {@code null}.
     *
     * @param workers Threads to interrupt.
     */
    public static void interrupt(Iterable<? extends Thread> workers) {
        if (workers != null)
            for (Thread worker : workers)
                worker.interrupt();
    }

    /**
     * Waits for completion of a given thread. If thread is {@code null} then
     * this method returns immediately returning {@code true}
     *
     * @param t Thread to join.
     * @param log Logger for logging errors.
     * @return {@code true} if thread has finished, {@code false} otherwise.
     */
    public static boolean join(@Nullable Thread t, @Nullable GridLogger log) {
        if (t != null)
            try {
                t.join();

                return true;
            }
            catch (InterruptedException ignore) {
                warn(log, "Got interrupted while waiting for completion of a thread: " + t);

                Thread.currentThread().interrupt();

                return false;
            }

        return true;
    }

    /**
     * Waits for completion of a given threads. If thread is {@code null} then
     * this method returns immediately returning {@code true}
     *
     * @param workers Thread to join.
     * @param log Logger for logging errors.
     * @return {@code true} if thread has finished, {@code false} otherwise.
     */
    public static boolean joinThreads(Iterable<? extends Thread> workers, @Nullable GridLogger log) {
        boolean retval = true;

        if (workers != null)
            for (Thread worker : workers)
                if (!join(worker, log))
                    retval = false;

        return retval;
    }

    /**
     * Starts given threads.
     *
     * @param threads Threads to start.
     */
    public static void startThreads(Iterable<? extends Thread> threads) {
        if (threads != null) {
            for (Thread thread : threads) {
                if (thread != null)
                    thread.start();
            }
        }
    }

    /**
     * Cancels given runnable.
     *
     * @param w Worker to cancel - it's no-op if runnable is {@code null}.
     */
    public static void cancel(@Nullable GridWorker w) {
        if (w != null)
            w.cancel();
    }

    /**
     * Cancels collection of runnables.
     *
     * @param ws Collection of workers - it's no-op if collection is {@code null}.
     */
    public static void cancel(Iterable<? extends GridWorker> ws) {
        if (ws != null)
            for (GridWorker w : ws)
                w.cancel();
    }

    /**
     * Joins runnable.
     *
     * @param w Worker to join.
     * @param log The logger to possible exception.
     * @return {@code true} if worker has not been interrupted, {@code false} if it was interrupted.
     */
    public static boolean join(@Nullable GridWorker w, @Nullable GridLogger log) {
        if (w != null)
            try {
                w.join();
            }
            catch (InterruptedException ignore) {
                warn(log, "Got interrupted while waiting for completion of runnable: " + w);

                Thread.currentThread().interrupt();

                return false;
            }

        return true;
    }

    /**
     * Joins given collection of runnables.
     *
     * @param ws Collection of workers to join.
     * @param log The logger to possible exceptions.
     * @return {@code true} if none of the worker have been interrupted,
     *      {@code false} if at least one was interrupted.
     */
    public static boolean join(Iterable<? extends GridWorker> ws, GridLogger log) {
        boolean retval = true;

        if (ws != null)
            for (GridWorker w : ws)
                if (!join(w, log))
                    retval = false;

        return retval;
    }

    /**
     * Shutdowns given {@code ExecutorService} and wait for executor service to stop.
     *
     * @param owner The ExecutorService owner.
     * @param exec ExecutorService to shutdown.
     * @param log The logger to possible exceptions and warnings.
     */
    public static void shutdownNow(Class<?> owner, @Nullable ExecutorService exec, @Nullable GridLogger log) {
        if (exec != null) {
            List<Runnable> tasks = exec.shutdownNow();

            if (!F.isEmpty(tasks))
                U.warn(log, "Runnable tasks outlived thread pool executor service [owner=" + getSimpleName(owner) +
                    ", tasks=" + tasks + ']');

            try {
                exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException ignored) {
                warn(log, "Got interrupted while waiting for executor service to stop.");

                exec.shutdownNow();

                // Preserve interrupt status.
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Creates appropriate empty projection exception.
     *
     * @return Empty projection exception.
     */
    public static GridEmptyProjectionException emptyTopologyException() {
        return new GridEmptyProjectionException("Topology projection is empty. Note that predicate based " +
            "projection can be empty from call to call.");
    }

    /**
     * Writes UUIDs to output stream. This method is meant to be used by
     * implementations of {@link Externalizable} interface.
     *
     * @param out Output stream.
     * @param col UUIDs to write.
     * @throws IOException If write failed.
     */
    public static void writeUuids(DataOutput out, @Nullable Collection<UUID> col) throws IOException {
        if (col != null) {
            out.writeInt(col.size());

            for (UUID id : col)
                writeUuid(out, id);
        }
        else
            out.writeInt(-1);
    }

    /**
     * Reads UUIDs from input stream. This method is meant to be used by
     * implementations of {@link Externalizable} interface.
     *
     * @param in Input stream.
     * @return Read UUIDs.
     * @throws IOException If read failed.
     */
    @Nullable public static List<UUID> readUuids(DataInput in) throws IOException {
        int size = in.readInt();

        // Check null flag.
        if (size == -1)
            return null;

        List<UUID> col = new ArrayList<>(size);

        for (int i = 0; i < size; i++)
            col.add(readUuid(in));

        return col;
    }

    /**
     * Writes Grid UUIDs to output stream. This method is meant to be used by
     * implementations of {@link Externalizable} interface.
     *
     * @param out Output stream.
     * @param col Grid UUIDs to write.
     * @throws IOException If write failed.
     */
    public static void writeGridUuids(DataOutput out, @Nullable Collection<GridUuid> col) throws IOException {
        if (col != null) {
            out.writeBoolean(true);

            out.writeInt(col.size());

            for (GridUuid id : col)
                writeGridUuid(out, id);
        }
        else
            out.writeBoolean(false);
    }

    /**
     * Reads Grid UUIDs from input stream. This method is meant to be used by
     * implementations of {@link Externalizable} interface.
     *
     * @param in Input stream.
     * @return Read Grid UUIDs.
     * @throws IOException If read failed.
     */
    @Nullable public static List<GridUuid> readGridUuids(DataInput in) throws IOException {
        List<GridUuid> col = null;

        // Check null flag.
        if (in.readBoolean()) {
            int size = in.readInt();

            col = new ArrayList<>(size);

            for (int i = 0; i < size; i++)
                col.add(readGridUuid(in));
        }

        return col;
    }

    /**
     * Writes UUID to output stream. This method is meant to be used by
     * implementations of {@link Externalizable} interface.
     *
     * @param out Output stream.
     * @param uid UUID to write.
     * @throws IOException If write failed.
     */
    public static void writeUuid(DataOutput out, UUID uid) throws IOException {
        // Write null flag.
        out.writeBoolean(uid == null);

        if (uid != null) {
            out.writeLong(uid.getMostSignificantBits());
            out.writeLong(uid.getLeastSignificantBits());
        }
    }

    /**
     * Reads UUID from input stream. This method is meant to be used by
     * implementations of {@link Externalizable} interface.
     *
     * @param in Input stream.
     * @return Read UUID.
     * @throws IOException If read failed.
     */
    @Nullable public static UUID readUuid(DataInput in) throws IOException {
        // If UUID is not null.
        if (!in.readBoolean()) {
            long most = in.readLong();
            long least = in.readLong();

            return GridUuidCache.onGridUuidRead(new UUID(most, least));
        }

        return null;
    }

    /**
     * Writes {@link GridUuid} to output stream. This method is meant to be used by
     * implementations of {@link Externalizable} interface.
     *
     * @param out Output stream.
     * @param uid UUID to write.
     * @throws IOException If write failed.
     */
    public static void writeGridUuid(DataOutput out, GridUuid uid) throws IOException {
        // Write null flag.
        out.writeBoolean(uid == null);

        if (uid != null) {
            out.writeLong(uid.globalId().getMostSignificantBits());
            out.writeLong(uid.globalId().getLeastSignificantBits());

            out.writeLong(uid.localId());
        }
    }

    /**
     * Reads {@link GridUuid} from input stream. This method is meant to be used by
     * implementations of {@link Externalizable} interface.
     *
     * @param in Input stream.
     * @return Read UUID.
     * @throws IOException If read failed.
     */
    @Nullable public static GridUuid readGridUuid(DataInput in) throws IOException {
        // If UUID is not null.
        if (!in.readBoolean()) {
            long most = in.readLong();
            long least = in.readLong();

            UUID globalId = GridUuidCache.onGridUuidRead(new UUID(most, least));

            long locId = in.readLong();

            return new GridUuid(globalId, locId);
        }

        return null;
    }

    /**
     * Converts GridUuid to bytes.
     *
     * @param uuid GridUuid to convert.
     * @param out Output array to write to.
     * @param off Offset from which to write.
     */
    public static void gridUuidToBytes(GridUuid uuid, byte[] out, int off) {
        assert uuid != null;

        U.longToBytes(uuid.globalId().getMostSignificantBits(), out, off);
        U.longToBytes(uuid.globalId().getLeastSignificantBits(), out, off + 8);
        U.longToBytes(uuid.localId(), out, off + 16);
    }

    /**
     * Converts bytes to GridUuid.
     *
     * @param in Input byte array.
     * @param off Offset from which start reading.
     * @return GridUuid instance.
     */
    public static GridUuid bytesToGridUuid(byte[] in, int off) {
        long most = U.bytesToLong(in, off);
        long least = U.bytesToLong(in, off + 8);
        long locId = U.bytesToLong(in, off + 16);

        return new GridUuid(GridUuidCache.onGridUuidRead(new UUID(most, least)), locId);
    }

    /**
     * Writes byte array to output stream accounting for <tt>null</tt> values.
     *
     * @param out Output stream to write to.
     * @param arr Array to write, possibly <tt>null</tt>.
     * @throws IOException If write failed.
     */
    public static void writeByteArray(DataOutput out, @Nullable byte[] arr) throws IOException {
        if (arr == null)
            out.writeInt(-1);
        else {
            out.writeInt(arr.length);

            out.write(arr);
        }
    }

    /**
     * Writes byte array to output stream accounting for <tt>null</tt> values.
     *
     * @param out Output stream to write to.
     * @param arr Array to write, possibly <tt>null</tt>.
     * @throws IOException If write failed.
     */
    public static void writeByteArray(DataOutput out, @Nullable byte[] arr, int maxLen) throws IOException {
        if (arr == null)
            out.writeInt(-1);
        else {
            int len = Math.min(arr.length, maxLen);

            out.writeInt(len);

            out.write(arr, 0, len);
        }
    }

    /**
     * Reads byte array from input stream accounting for <tt>null</tt> values.
     *
     * @param in Stream to read from.
     * @return Read byte array, possibly <tt>null</tt>.
     * @throws IOException If read failed.
     */
    @Nullable public static byte[] readByteArray(DataInput in) throws IOException {
        int len = in.readInt();

        if (len == -1)
            return null; // Value "-1" indicates null.

        byte[] res = new byte[len];

        in.readFully(res);

        return res;
    }

    /**
     * Writes boolean array to output stream accounting for <tt>null</tt> values.
     *
     * @param out Output stream to write to.
     * @param arr Array to write, possibly <tt>null</tt>.
     * @throws IOException If write failed.
     */
    public static void writeBooleanArray(DataOutput out, @Nullable boolean[] arr) throws IOException {
        if (arr == null)
            out.writeInt(-1);
        else {
            out.writeInt(arr.length);

            for (boolean b : arr)
                out.writeBoolean(b);
        }
    }

    /**
     * Writes int array to output stream accounting for <tt>null</tt> values.
     *
     * @param out Output stream to write to.
     * @param arr Array to write, possibly <tt>null</tt>.
     * @throws IOException If write failed.
     */
    public static void writeIntArray(DataOutput out, @Nullable int[] arr) throws IOException {
        if (arr == null)
            out.writeInt(-1);
        else {
            out.writeInt(arr.length);

            for (int b : arr)
                out.writeInt(b);
        }
    }

    /**
     * Reads boolean array from input stream accounting for <tt>null</tt> values.
     *
     * @param in Stream to read from.
     * @return Read byte array, possibly <tt>null</tt>.
     * @throws IOException If read failed.
     */
    @Nullable public static boolean[] readBooleanArray(DataInput in) throws IOException {
        int len = in.readInt();

        if (len == -1)
            return null; // Value "-1" indicates null.

        boolean[] res = new boolean[len];

        for (int i = 0; i < len; i++)
            res[i] = in.readBoolean();

        return res;
    }

    /**
     * Reads int array from input stream accounting for <tt>null</tt> values.
     *
     * @param in Stream to read from.
     * @return Read byte array, possibly <tt>null</tt>.
     * @throws IOException If read failed.
     */
    @Nullable public static int[] readIntArray(DataInput in) throws IOException {
        int len = in.readInt();

        if (len == -1)
            return null; // Value "-1" indicates null.

        int[] res = new int[len];

        for (int i = 0; i < len; i++)
            res[i] = in.readInt();

        return res;
    }

    /**
     * Reads byte array from given buffers (changing buffer positions).
     *
     * @param bufs Byte buffers.
     * @return Byte array.
     */
    public static byte[] readByteArray(ByteBuffer... bufs) {
        assert !F.isEmpty(bufs);

        int size = 0;

        for (ByteBuffer buf : bufs)
            size += buf.remaining();

        byte[] res = new byte[size];

        int off = 0;

        for (ByteBuffer buf : bufs) {
            int len = buf.remaining();

            if (len != 0) {
                buf.get(res, off, len);

                off += len;
            }
        }

        assert off == res.length;

        return res;
    }

    /**
     * Calculates hash code for the given byte buffers contents. Compatible with {@link Arrays#hashCode(byte[])}
     * with the same content. Does not change buffers positions.
     *
     * @param bufs Byte buffers.
     * @return Hash code.
     */
    public static int hashCode(ByteBuffer... bufs) {
        int res = 1;

        for (ByteBuffer buf : bufs) {
            int pos = buf.position();

            while (buf.hasRemaining())
                res = 31 * res + buf.get();

            buf.position(pos);
        }

        return res;
    }

    /**
     * @param out Output.
     * @param map Map to write.
     * @throws IOException If write failed.
     */
    public static void writeMap(ObjectOutput out, Map<?, ?> map) throws IOException {
        if (map != null) {
            out.writeInt(map.size());

            for (Map.Entry<?, ?> e : map.entrySet()) {
                out.writeObject(e.getKey());
                out.writeObject(e.getValue());
            }
        }
        else
            out.writeInt(-1);
    }

    /**
     *
     * @param in Input.
     * @return Read map.
     * @throws IOException If de-serialization failed.
     * @throws ClassNotFoundException If deserialized class could not be found.
     */
    @SuppressWarnings({"unchecked"})
    @Nullable public static <K, V> Map<K, V> readMap(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();

        if (size == -1)
            return null;

        Map<K, V> map = new HashMap<>(size, 1.0f);

        for (int i = 0; i < size; i++)
            map.put((K)in.readObject(), (V)in.readObject());

        return map;
    }

    /**
     * @param in Input.
     * @return Read map.
     * @throws IOException If de-serialization failed.
     * @throws ClassNotFoundException If deserialized class could not be found.
     */
    @SuppressWarnings({"unchecked"})
    @Nullable public static <K, V> TreeMap<K, V> readTreeMap(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();

        if (size == -1)
            return null;

        TreeMap<K, V> map = new TreeMap<>();

        for (int i = 0; i < size; i++)
            map.put((K)in.readObject(), (V)in.readObject());

        return map;
    }

    /**
     * Writes string-to-string map to given data output.
     *
     * @param out Data output.
     * @param map Map.
     * @throws IOException If write failed.
     */
    public static void writeStringMap(DataOutput out, @Nullable Map<String, String> map) throws IOException {
        if (map != null) {
            out.writeInt(map.size());

            for (Map.Entry<String, String> e : map.entrySet()) {
                out.writeUTF(e.getKey());
                out.writeUTF(e.getValue());
            }
        }
        else
            out.writeInt(-1);
    }

    /**
     * Reads string-to-string map written by {@link #writeStringMap(DataOutput, Map)}.
     *
     * @param in Data input.
     * @throws IOException If write failed.
     * @return Read result.
     */
    public static Map<String, String> readStringMap(DataInput in) throws IOException {
        int size = in.readInt();

        if (size == -1)
            return null;
        else {
            Map<String, String> map = U.newHashMap(size);

            for (int i = 0; i < size; i++)
                map.put(in.readUTF(), in.readUTF());

            return map;
        }
    }

    /**
     *
     * @param in Input.
     * @return Read map.
     * @throws IOException If de-serialization failed.
     * @throws ClassNotFoundException If deserialized class could not be found.
     */
    @SuppressWarnings({"unchecked"})
    @Nullable public static <K, V> LinkedHashMap<K, V> readLinkedMap(ObjectInput in)
        throws IOException, ClassNotFoundException {
        int size = in.readInt();

        // Check null flag.
        if (size == -1)
            return null;

        LinkedHashMap<K, V> map = new LinkedHashMap<>(size, 1.0f);

        for (int i = 0; i < size; i++)
            map.put((K)in.readObject(), (V)in.readObject());

        return map;
    }

    /**
     * @param out Output.
     * @param map Map to write.
     * @throws IOException If write failed.
     */
    public static void writeIntKeyMap(ObjectOutput out, Map<Integer, ?> map) throws IOException {
        if (map != null) {
            out.writeInt(map.size());

            for (Map.Entry<Integer, ?> e : map.entrySet()) {
                out.writeInt(e.getKey());
                out.writeObject(e.getValue());
            }
        }
        else
            out.writeInt(-1);
    }

    /**
     * @param in Input.
     * @return Read map.
     * @throws IOException If de-serialization failed.
     * @throws ClassNotFoundException If deserialized class could not be found.
     */
    @SuppressWarnings({"unchecked"})
    @Nullable public static <V> Map<Integer, V> readIntKeyMap(ObjectInput in) throws IOException,
        ClassNotFoundException {
        int size = in.readInt();

        // Check null flag.
        if (size == -1)
            return null;

        Map<Integer, V> map = new HashMap<>(size, 1.0f);

        for (int i = 0; i < size; i++)
            map.put(in.readInt(), (V)in.readObject());

        return map;
    }

    /**
     * @param out Output.
     * @param map Map to write.
     * @throws IOException If write failed.
     */
    public static void writeIntKeyIntValueMap(DataOutput out, Map<Integer, Integer> map) throws IOException {
        if (map != null) {
            out.writeBoolean(true);

            out.writeInt(map.size());

            for (Map.Entry<Integer, Integer> e : map.entrySet()) {
                out.writeInt(e.getKey());
                out.writeInt(e.getValue());
            }
        }
        else
            out.writeBoolean(false);
    }

    /**
     * @param in Input.
     * @return Read map.
     * @throws IOException If de-serialization failed.
     */
    @SuppressWarnings({"unchecked"})
    @Nullable public static Map<Integer, Integer> readIntKeyIntValueMap(DataInput in) throws IOException {
        Map<Integer, Integer> map = null;

        // Check null flag.
        if (in.readBoolean()) {
            int size = in.readInt();

            map = new HashMap<>(size, 1.0f);

            for (int i = 0; i < size; i++)
                map.put(in.readInt(), in.readInt());
        }

        return map;
    }

    /**
     * @param in Input.
     * @return Deserialized list.
     * @throws IOException If deserialization failed.
     * @throws ClassNotFoundException If deserialized class could not be found.
     */
    @SuppressWarnings({"unchecked"})
    @Nullable public static <E> List<E> readList(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();

        // Check null flag.
        if (size == -1)
            return null;

        List<E> col = new ArrayList<>(size);

        for (int i = 0; i < size; i++)
            col.add((E)in.readObject());

        return col;
    }

    /**
     * @param in Input.
     * @return Deserialized list.
     * @throws IOException If deserialization failed.
     */
    @Nullable public static List<Integer> readIntList(DataInput in) throws IOException {
        int size = in.readInt();

        // Check null flag.
        if (size == -1)
            return null;

        List<Integer> col = new ArrayList<>(size);

        for (int i = 0; i < size; i++)
            col.add(in.readInt());

        return col;
    }

    /**
     * // FIXME: added for DR dataCenterIds, review if it is needed after GG-6879.
     *
     * @param out Output.
     * @param col Set to write.
     * @throws IOException If write failed.
     */
    public static void writeByteCollection(DataOutput out, Collection<Byte> col) throws IOException {
        if (col != null) {
            out.writeInt(col.size());

            for (Byte i : col)
                out.writeByte(i);
        }
        else
            out.writeInt(-1);
    }

    /**
     * // FIXME: added for DR dataCenterIds, review if it is needed after GG-6879.
     *
     * @param in Input.
     * @return Deserialized list.
     * @throws IOException If deserialization failed.
     */
    @Nullable public static List<Byte> readByteList(DataInput in) throws IOException {
        int size = in.readInt();

        // Check null flag.
        if (size == -1)
            return null;

        List<Byte> col = new ArrayList<>(size);

        for (int i = 0; i < size; i++)
            col.add(in.readByte());

        return col;
    }

    /**
     * @param in Input.
     * @return Deserialized set.
     * @throws IOException If deserialization failed.
     * @throws ClassNotFoundException If deserialized class could not be found.
     */
    @SuppressWarnings({"unchecked"})
    @Nullable public static <E> Set<E> readSet(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();

        // Check null flag.
        if (size == -1)
            return null;

        Set<E> set = new HashSet(size, 1.0f);

        for (int i = 0; i < size; i++)
            set.add((E) in.readObject());

        return set;
    }

    /**
     * @param in Input.
     * @return Deserialized set.
     * @throws IOException If deserialization failed.
     */
    @Nullable public static Set<Integer> readIntSet(DataInput in) throws IOException {
        int size = in.readInt();

        // Check null flag.
        if (size == -1)
            return null;

        Set<Integer> set = new HashSet<>(size, 1.0f);

        for (int i = 0; i < size; i++)
            set.add(in.readInt());

        return set;
    }

    /**
     * Writes string to output stream accounting for {@code null} values.
     *
     * @param out Output stream to write to.
     * @param s String to write, possibly {@code null}.
     * @throws IOException If write failed.
     */
    public static void writeString(DataOutput out, String s) throws IOException {
        // Write null flag.
        out.writeBoolean(s == null);

        if (s != null)
            out.writeUTF(s);
    }

    /**
     * Reads string from input stream accounting for {@code null} values.
     *
     * @param in Stream to read from.
     * @return Read string, possibly {@code null}.
     * @throws IOException If read failed.
     */
    @Nullable public static String readString(DataInput in) throws IOException {
        // If value is not null, then read it. Otherwise return null.
        return !in.readBoolean() ? in.readUTF() : null;
    }

    /**
     * Writes enum to output stream accounting for {@code null} values.
     * Note: method writes only one byte for every enum. Therefore, this method
     * only for Enums with maximum count of values equals to 128.
     *
     * @param out Output stream to write to.
     * @param e Enum value to write, possibly {@code null}.
     * @throws IOException If write failed.
     */
    public static <E extends Enum> void writeEnum(DataOutput out, E e) throws IOException {
        out.writeByte(e == null ? -1 : e.ordinal());
    }

    /**
     * Writes enum to output stream accounting for {@code null} values.
     *
     * @param out Output stream to write to.
     * @param e Enum value to write, possibly {@code null}.
     * @throws IOException If write failed.
     *
     * @deprecated Need to remove when release will not be to support
     * backward compatible. Use {@code U.writeEnum(DataOutput, Enum)}.
     */
    @Deprecated
    public static <E extends Enum> void writeEnum0(DataOutput out, E e) throws IOException {
        out.writeBoolean(e == null);

        if (e != null)
            out.writeInt(e.ordinal());
    }

    /**
     * Reads enum ordinal from input stream accounting for {@code null} values.
     *
     * @param in Stream to read from.
     * @return Read enum ordinal, possibly {@code -1} means {@code null}.
     * @throws IOException If read failed.
     *
     * @deprecated Need to remove when release will not be to support
     * backward compatible. Use {@code Enum.fromOrdinal(int)}.
     */
    @Deprecated
    public static int readEnumOrdinal0(DataInput in) throws IOException {
        return !in.readBoolean() ? in.readInt() : -1;
    }

    /**
     * Gets collection value by index.
     *
     * @param vals Collection of values.
     * @param idx Index of value in the collection.
     * @param <T> Type of collection values.
     * @return Value at the given index.
     */
    public static <T> T getByIndex(Collection<T> vals, int idx) {
        assert idx < vals.size();

        int i = 0;

        for (T val : vals) {
            if (idx == i)
                return val;

            i++;
        }

        assert false : "Should never be reached.";

        return null;
    }

    /**
     * Gets annotation for a class.
     *
     * @param <T> Type of annotation to return.
     * @param cls Class to get annotation from.
     * @param annCls Annotation to get.
     * @return Instance of annotation, or {@code null} if not found.
     */
    @Nullable public static <T extends Annotation> T getAnnotation(Class<?> cls, Class<T> annCls) {
        if (cls == Object.class)
            return null;

        T ann = cls.getAnnotation(annCls);

        if (ann != null)
            return ann;

        for (Class<?> itf : cls.getInterfaces()) {
            ann = getAnnotation(itf, annCls); // Recursion.

            if (ann != null)
                return ann;
        }

        if (!cls.isInterface()) {
            ann = getAnnotation(cls.getSuperclass(), annCls);

            if (ann != null)
                return ann;
        }

        return null;
    }

    /**
     * Indicates if class has given annotation.
     *
     * @param <T> Annotation type.
     * @param cls Class to get annotation from.
     * @param annCls Annotation to get.
     * @return {@code true} if class has annotation or {@code false} otherwise.
     */
    public static <T extends Annotation> boolean hasAnnotation(Class<?> cls, Class<T> annCls) {
        return getAnnotation(cls, annCls) != null;
    }

    /**
     * Indicates if class has given annotation.
     *
     * @param o Object to get annotation from.
     * @param annCls Annotation to get.
     * @return {@code true} if class has annotation or {@code false} otherwise.
     */
    public static <T extends Annotation> boolean hasAnnotation(Object o, Class<T> annCls) {
        return o != null && hasAnnotation(o.getClass(), annCls);
    }

    /**
     * Gets simple class name taking care of empty names.
     *
     * @param cls Class to get the name for.
     * @return Simple class name.
     */
    public static String getSimpleName(Class<?> cls) {
        String name = cls.getSimpleName();

        if (F.isEmpty(name))
            name = cls.getName().substring(cls.getPackage().getName().length() + 1);

        return name;
    }

    /**
     * Checks if the map passed in is contained in base map.
     *
     * @param base Base map.
     * @param map Map to check.
     * @return {@code True} if all entries within map are contained in base map,
     *      {@code false} otherwise.
     */
    @SuppressWarnings({"SuspiciousMethodCalls"})
    public static boolean containsAll(Map<?, ?> base, Map<?, ?> map) {
        assert base != null;
        assert map != null;

        for (Map.Entry<?, ?> entry : map.entrySet())
            if (base.containsKey(entry.getKey())) {
                Object val = base.get(entry.getKey());

                if (val == null && entry.getValue() == null)
                    continue;

                if (val == null || entry.getValue() == null || !val.equals(entry.getValue()))
                    // Mismatch found.
                    return false;
            }
            else
                return false;

        // All entries in 'map' are contained in base map.
        return true;
    }

    /**
     * Gets task name for the given task class.
     *
     * @param taskCls Task class.
     * @return Either task name from class annotation (see {@link org.gridgain.grid.compute.GridComputeTaskName}})
     *      or task class name if there is no annotation.
     */
    public static String getTaskName(Class<? extends GridComputeTask<?, ?>> taskCls) {
        GridComputeTaskName nameAnn = getAnnotation(taskCls, GridComputeTaskName.class);

        return nameAnn == null ? taskCls.getName() : nameAnn.value();
    }

    /**
     * Creates SPI attribute name by adding prefix to the attribute name.
     * Prefix is an SPI name + '.'.
     *
     * @param spi SPI.
     * @param attrName attribute name.
     * @return SPI attribute name.
     */
    public static String spiAttribute(GridSpi spi, String attrName) {
        assert spi != null;
        assert spi.getName() != null;

        return spi.getName() + '.' + attrName;
    }

    /**
     * Gets resource path for the class.
     *
     * @param clsName Class name.
     * @return Resource name for the class.
     */
    public static String classNameToResourceName(String clsName) {
        return clsName.replaceAll("\\.", "/") + ".class";
    }

    /**
     * Gets runtime MBean.
     *
     * @return Runtime MBean.
     */
    public static RuntimeMXBean getRuntimeMx() {
        return ManagementFactory.getRuntimeMXBean();
    }

    /**
     * Gets threading MBean.
     *
     * @return Threading MBean.
     */
    public static ThreadMXBean getThreadMx() {
        return ManagementFactory.getThreadMXBean();
    }

    /**
     * Gets OS MBean.
     * @return OS MBean.
     */
    public static OperatingSystemMXBean getOsMx() {
        return ManagementFactory.getOperatingSystemMXBean();
    }

    /**
     * Gets memory MBean.
     *
     * @return Memory MBean.
     */
    public static MemoryMXBean getMemoryMx() {
        return ManagementFactory.getMemoryMXBean();
    }

    /**
     * Gets compilation MBean.
     *
     * @return Compilation MBean.
     */
    public static CompilationMXBean getCompilerMx() {
        return ManagementFactory.getCompilationMXBean();
    }

    /**
     * Tries to detect user class from passed in object inspecting
     * collections, arrays or maps.
     *
     * @param obj Object.
     * @return First non-JDK or deployment aware class or passed in object class.
     */
    public static Class<?> detectClass(Object obj) {
        assert obj != null;

        if (obj instanceof GridPeerDeployAware)
            return ((GridPeerDeployAware)obj).deployClass();

        if (U.isPrimitiveArray(obj))
            return obj.getClass();

        if (!U.isJdk(obj.getClass()))
            return obj.getClass();

        if (obj instanceof Iterable<?>) {
            Object o = F.first((Iterable<?>)obj);

            // No point to continue, if null.
            return o != null ? o.getClass() : obj.getClass();
        }

        if (obj instanceof Map) {
            Map.Entry<?, ?> e = F.firstEntry((Map<?, ?>)obj);

            if (e != null) {
                Object k = e.getKey();

                if (k != null && !U.isJdk(k.getClass()))
                    return k.getClass();

                Object v = e.getValue();

                return v != null ? v.getClass() : obj.getClass();
            }
        }

        if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);

            if (len > 0) {
                Object o = Array.get(obj, 0);

                return o != null ? o.getClass() : obj.getClass();
            }
            else
                return obj.getClass().getComponentType();
        }

        return obj.getClass();
    }

    /**
     * Detects class loader for given class.
     * <p>
     * This method will first check if {@link Thread#getContextClassLoader()} is appropriate.
     * If yes, then context class loader will be returned, otherwise
     * the {@link Class#getClassLoader()} will be returned.
     *
     * @param cls Class to find class loader for.
     * @return Class loader for given class (never {@code null}).
     */
    public static ClassLoader detectClassLoader(Class<?> cls) {
        return GridClassLoaderCache.classLoader(cls);
    }

    /**
     * Detects class loader for given object's class.
     *
     * @param obj Object to find class loader for class of.
     * @return Class loader for given object (possibly {@code null}).
     */
    @Nullable public static ClassLoader detectObjectClassLoader(@Nullable Object obj) {
        if (obj == null)
            return null;

        if (obj instanceof GridPeerDeployAware)
            return ((GridPeerDeployAware)obj).classLoader();

        return detectClassLoader(obj.getClass());
    }

    /**
     * Tests whether or not given class is loadable provided class loader.
     *
     * @param clsName Class name to test.
     * @param ldr Class loader to test with. If {@code null} - we'll use system class loader instead.
     *      If System class loader is not set - this method will return {@code false}.
     * @return {@code True} if class is loadable, {@code false} otherwise.
     */
    public static boolean isLoadableBy(String clsName, @Nullable ClassLoader ldr) {
        assert clsName != null;

        if (ldr == null)
            ldr = gridClassLoader;

        String lambdaParent = U.lambdaEnclosingClassName(clsName);

        try {
            ldr.loadClass(lambdaParent == null ? clsName : lambdaParent);

            return true;
        }
        catch (ClassNotFoundException ignore) {
            return false;
        }
    }

    /**
     * Gets the peer deploy aware instance for the object with the widest class loader.
     * If collection is {@code null}, empty or contains only {@code null}s - the peer
     * deploy aware object based on system class loader will be returned.
     *
     * @param c Collection.
     * @return Peer deploy aware object from this collection with the widest class loader.
     * @throws IllegalArgumentException Thrown in case when common class loader for all
     *      elements in this collection cannot be found. In such case - peer deployment
     *      is not possible.
     */
    public static GridPeerDeployAware peerDeployAware0(@Nullable Iterable<?> c) {
        if (!F.isEmpty(c)) {
            assert c != null;

            // We need to find common classloader for all elements AND the collection itself
            Collection<Object> tmpC = new ArrayList<>();

            for (Object e: c)
                tmpC.add(e);

            tmpC.add(c);

            boolean notAllNulls = false;

            for (Object obj : tmpC) {
                if (obj != null) {
                    notAllNulls = true;

                    if (hasCommonClassLoader(obj, tmpC))
                        return obj == c ? peerDeployAware(obj) : peerDeployAware0(obj);
                }
            }

            // If all are nulls - don't throw an exception.
            if (notAllNulls)
                throw new IllegalArgumentException("Failed to find common class loader for all elements in " +
                    "given collection. Peer deployment cannot be performed for such collection.");
        }

        return peerDeployAware(c);
    }

    /**
     * Check if all elements from the collection could be loaded with the same classloader as the given object.
     *
     * @param obj base object.
     * @param c collection to check elements from.
     * @return {@code true} if all elements could be loaded with {@code obj}'s classloader, {@code false} otherwise
     */
    private static boolean hasCommonClassLoader(Object obj, Iterable<?> c) {
        assert obj != null;
        assert c != null;

        ClassLoader ldr = obj instanceof GridPeerDeployAware ?
            ((GridPeerDeployAware)obj).classLoader() : detectClassLoader(obj.getClass());

        boolean found = true;

        for (Object obj2 : c) {
            if (obj2 == null || obj2 == obj)
                continue;

            // Obj2 class name.
            String clsName = obj2 instanceof GridPeerDeployAware ?
                ((GridPeerDeployAware)obj2).deployClass().getName() : obj2.getClass().getName();

            if (!isLoadableBy(clsName, ldr)) {
                found = false;

                break;
            }
        }

        return found;
    }

    /**
     * Gets the peer deploy aware instance for the object with the widest class loader.
     * If array is {@code null}, empty or contains only {@code null}s - the peer
     * deploy aware object based on system class loader will be returned.
     *
     * @param c Objects.
     * @return Peer deploy aware object from this array with the widest class loader.
     * @throws IllegalArgumentException Thrown in case when common class loader for all
     *      elements in this array cannot be found. In such case - peer deployment
     *      is not possible.
     */
    @SuppressWarnings({"ZeroLengthArrayAllocation"})
    public static GridPeerDeployAware peerDeployAware0(@Nullable Object... c) {
        if (!F.isEmpty(c)) {
            assert c != null;

            boolean notAllNulls = false;

            for (Object obj : c) {
                if (obj != null) {
                    notAllNulls = true;

                    ClassLoader ldr = obj instanceof GridPeerDeployAware ?
                        ((GridPeerDeployAware)obj).classLoader() : obj.getClass().getClassLoader();

                    boolean found = true;

                    for (Object obj2 : c) {
                        if (obj2 == null || obj2 == obj)
                            continue;

                        // Obj2 class name.
                        String clsName = obj2 instanceof GridPeerDeployAware ?
                            ((GridPeerDeployAware)obj2).deployClass().getName() : obj2.getClass().getName();

                        if (!isLoadableBy(clsName, ldr)) {
                            found = false;

                            break;
                        }
                    }

                    if (found)
                        return peerDeployAware0(obj);
                }
            }

            // If all are nulls - don't throw an exception.
            if (notAllNulls)
                throw new IllegalArgumentException("Failed to find common class loader for all elements in " +
                    "given collection. Peer deployment cannot be performed for such collection.");
        }

        return peerDeployAware(new Object[0]);
    }

    /**
     * Creates an instance of {@link GridPeerDeployAware} for object.
     *
     * Checks, if the object is an instance of collection or object
     * array.
     *
     * @param obj Object to deploy.
     * @return {@link GridPeerDeployAware} instance for given object.
     */
    public static GridPeerDeployAware peerDeployAware0(Object obj) {
        if (obj instanceof Iterable)
            return peerDeployAware0((Iterable)obj);

        if (obj.getClass().isArray() && !U.isPrimitiveArray(obj))
            return peerDeployAware0((Object[])obj);

        return peerDeployAware(obj);
    }

    /**
     * Creates an instance of {@link GridPeerDeployAware} for object.
     *
     * @param obj Object to deploy.
     * @return {@link GridPeerDeployAware} instance for given object.
     */
    public static GridPeerDeployAware peerDeployAware(Object obj) {
        assert obj != null;

        if (obj instanceof GridPeerDeployAware)
            return (GridPeerDeployAware)obj;

        final Class<?> cls = obj instanceof Class ? (Class)obj : obj.getClass();

        return new GridPeerDeployAware() {
            /** */
            private ClassLoader ldr;

            @Override public Class<?> deployClass() {
                return cls;
            }

            @Override public ClassLoader classLoader() {
                if (ldr == null)
                    ldr = detectClassLoader(cls);

                return ldr;
            }
        };
    }

    /**
     * Unwraps top level user class for wrapped objects.
     *
     * @param obj Object to check.
     * @return Top level user class.
     */
    public static GridPeerDeployAware detectPeerDeployAware(GridPeerDeployAware obj) {
        GridPeerDeployAware p = nestedPeerDeployAware(obj, true, new GridIdentityHashSet<>(3));

        // Pass in obj.getClass() to avoid infinite recursion.
        return p != null ? p : peerDeployAware(obj.getClass());
    }

    /**
     * Gets peer deploy class if there is any {@link GridPeerDeployAware} within reach.
     *
     * @param obj Object to check.
     * @param top Indicates whether object is top level or a nested field.
     * @param processed Set of processed objects to avoid infinite recursion.
     * @return Peer deploy class, or {@code null} if one could not be found.
     */
    @Nullable private static GridPeerDeployAware nestedPeerDeployAware(Object obj, boolean top, Set<Object> processed) {
        // Avoid infinite recursion.
        if (!processed.add(obj))
            return null;

        if (obj instanceof GridPeerDeployAware) {
            GridPeerDeployAware p = (GridPeerDeployAware)obj;

            if (!top && p.deployClass() != null)
                return p;

            for (Class<?> cls = obj.getClass(); !cls.equals(Object.class); cls = cls.getSuperclass()) {
                // Cache by class name instead of class to avoid infinite growth of the
                // caching map in case of multiple redeployment of the same class.
                GridBiTuple<Class<?>, Collection<Field>> tup = p2pFields.get(cls.getName());

                boolean cached = tup != null && tup.get1().equals(cls);

                Iterable<Field> fields = cached ? tup.get2() : Arrays.asList(cls.getDeclaredFields());

                if (!cached) {
                    tup = F.t2();

                    tup.set1(cls);
                }

                for (Field f : fields)
                    // Special handling for anonymous classes.
                    if (cached || f.getName().startsWith("this$") || f.getName().startsWith("val$")) {
                        if (!cached) {
                            f.setAccessible(true);

                            if (tup.get2() == null)
                                tup.set2(new LinkedList<Field>());

                            tup.get2().add(f);
                        }

                        try {
                            Object o = f.get(obj);

                            if (o != null) {
                                // Recursion.
                                p = nestedPeerDeployAware(o, false, processed);

                                if (p != null) {
                                    if (!cached)
                                        // Potentially replace identical value
                                        // stored by another thread.
                                        p2pFields.put(cls.getName(), tup);

                                    return p;
                                }
                            }
                        }
                        catch (IllegalAccessException ignored) {
                            return null;
                        }
                    }
            }
        }
        // Don't go into internal GridGain structures.
        else if (isGridGain(obj.getClass()))
            return null;
        else if (obj instanceof Iterable)
            for (Object o : (Iterable<?>)obj) {
                // Recursion.
                GridPeerDeployAware p = nestedPeerDeployAware(o, false, processed);

                if (p != null)
                    return p;
            }
        else if (obj.getClass().isArray()) {
            Class<?> type = obj.getClass().getComponentType();

            // We don't care about primitives or internal JDK types.
            if (!type.isPrimitive() && !isJdk(type)) {
                Object[] arr = (Object[])obj;

                for (Object o : arr) {
                    // Recursion.
                    GridPeerDeployAware p = nestedPeerDeployAware(o, false, processed);

                    if (p != null)
                        return p;
                }
            }
        }

        return null;
    }

    /**
     * Checks if given class is of {@code GridGain} type.
     *
     * @param cls Class to check.
     * @return {@code True} if given class is of {@code GridGain} type.
     */
    public static boolean isGridGain(Class<?> cls) {
        return cls.getName().startsWith("org.gridgain");
    }

    /**
     * Checks if given class is of {@code Grid} type.
     *
     * @param cls Class to check.
     * @return {@code True} if given class is of {@code Grid} type.
     */
    public static boolean isGrid(Class<?> cls) {
        return cls.getName().startsWith("org.gridgain.grid");
    }

    /**
     * Replaces all occurrences of {@code org.gridgain.} with {@code o.g.},
     * {@code org.gridgain.grid.} with {@code o.g.g.}, {@code org.gridgain.visor.} with {@code o.g.v.} and
     * {@code org.gridgain.scalar.} with {@code o.g.s.}.
     *
     * @param s String to replace in.
     * @return Replaces string.
     */
    public static String compact(String s) {
        return s.replace("org.gridgain.grid.", "o.g.g.").
            replace("org.gridgain.visor.", "o.g.v.").
            replace("org.gridgain.scalar.", "o.g.s.").
            replace("org.gridgain.", "o.g.");
    }

    /**
     * Check if given class is of JDK type.
     *
     * @param cls Class to check.
     * @return {@code True} if object is JDK type.
     */
    public static boolean isJdk(Class<?> cls) {
        if (cls.isPrimitive())
            return true;

        String s = cls.getName();

        return s.startsWith("java.") || s.startsWith("javax.");
    }

    /**
     * Converts {@link InterruptedException} to {@link GridException}.
     *
     * @param mux Mux to wait on.
     * @throws GridInterruptedException If interrupted.
     */
    @SuppressWarnings({"WaitNotInLoop", "WaitWhileNotSynced"})
    public static void wait(Object mux) throws GridInterruptedException {
        try {
            mux.wait();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new GridInterruptedException(e);
        }
    }

    /**
     * Unzip file to folder.
     *
     * @param zipFile ZIP file.
     * @param toDir Directory to unzip file content.
     * @param log Grid logger.
     * @throws IOException In case of error.
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public static void unzip(File zipFile, File toDir, @Nullable GridLogger log) throws IOException {
        ZipFile zip = null;

        try {
            zip = new ZipFile(zipFile);

            for (ZipEntry entry : asIterable(zip.entries())) {
                if (entry.isDirectory()) {
                    // Assume directories are stored parents first then children.
                    new File(toDir, entry.getName()).mkdirs();

                    continue;
                }

                InputStream in = null;
                OutputStream out = null;

                try {
                    in = zip.getInputStream(entry);

                    File outFile = new File(toDir, entry.getName());

                    if (!outFile.getParentFile().exists())
                        outFile.getParentFile().mkdirs();

                    out = new BufferedOutputStream(new FileOutputStream(outFile));

                    copy(in, out);
                }
                finally {
                    close(in, log);
                    close(out, log);
                }
            }
        }
        finally {
            if (zip != null)
                zip.close();
        }
    }

    /**
     * Gets OS JDK string.
     *
     * @return OS JDK string.
     */
    public static String osJdkString() {
        return osJdkStr;
    }

    /**
     * Gets OS string.
     *
     * @return OS string.
     */
    public static String osString() {
        return osStr;
    }

    /**
     * Gets JDK string.
     *
     * @return JDK string.
     */
    public static String jdkString() {
        return jdkStr;
    }

    /**
     * Indicates whether current OS is Linux flavor.
     *
     * @return {@code true} if current OS is Linux - {@code false} otherwise.
     */
    public static boolean isLinux() {
        return linux;
    }

    /**
     * Gets JDK name.
     * @return JDK name.
     */
    public static String jdkName() {
        return jdkName;
    }

    /**
     * Gets JDK vendor.
     *
     * @return JDK vendor.
     */
    public static String jdkVendor() {
        return jdkVendor;
    }

    /**
     * Gets JDK version.
     *
     * @return JDK version.
     */
    public static String jdkVersion() {
        return jdkVer;
    }

    /**
     * Gets OS CPU-architecture.
     *
     * @return OS CPU-architecture.
     */
    public static String osArchitecture() {
        return osArch;
    }

    /**
     * Gets underlying OS name.
     *
     * @return Underlying OS name.
     */
    public static String osName() {
        return osName;
    }

    /**
     * Gets underlying OS version.
     *
     * @return Underlying OS version.
     */
    public static String osVersion() {
        return osVer;
    }

    /**
     * Indicates whether current OS is Mac OS.
     *
     * @return {@code true} if current OS is Mac OS - {@code false} otherwise.
     */
    public static boolean isMacOs() {
        return mac;
    }

    /**
     * Indicates whether current OS is Netware.
     *
     * @return {@code true} if current OS is Netware - {@code false} otherwise.
     */
    public static boolean isNetWare() {
        return netware;
    }

    /**
     * Indicates whether current OS is Solaris.
     *
     * @return {@code true} if current OS is Solaris (SPARC or x86) - {@code false} otherwise.
     */
    public static boolean isSolaris() {
        return solaris;
    }

    /**
     * Indicates whether current OS is Solaris on Spark box.
     *
     * @return {@code true} if current OS is Solaris SPARC - {@code false} otherwise.
     */
    public static boolean isSolarisSparc() {
        return solaris && sparc;
    }

    /**
     * Indicates whether current OS is Solaris on x86 box.
     *
     * @return {@code true} if current OS is Solaris x86 - {@code false} otherwise.
     */
    public static boolean isSolarisX86() {
        return solaris && x86;
    }

    /**
     * Indicates whether current OS is UNIX flavor.
     *
     * @return {@code true} if current OS is UNIX - {@code false} otherwise.
     */
    public static boolean isUnix() {
        return unix;
    }

    /**
     * Indicates whether current OS is Windows.
     *
     * @return {@code true} if current OS is Windows (any versions) - {@code false} otherwise.
     */
    public static boolean isWindows() {
        return win7 || win8 || win81 || winXp || win95 || win98 || winNt || win2k ||
            win2003 || win2008 || winVista || unknownWin;
    }

    /**
     * Indicates whether current OS is Windows Vista.
     *
     * @return {@code true} if current OS is Windows Vista - {@code false} otherwise.
     */
    public static boolean isWindowsVista() {
        return winVista;
    }

    /**
     * Indicates whether current OS is Windows 7.
     *
     * @return {@code true} if current OS is Windows 7 - {@code false} otherwise.
     */
    public static boolean isWindows7() {
        return win7;
    }

    /**
     * Indicates whether current OS is Windows 8.
     *
     * @return {@code true} if current OS is Windows 8 - {@code false} otherwise.
     */
    public static boolean isWindows8() {
        return win8;
    }

    /**
     * Indicates whether current OS is Windows 8.1.
     *
     * @return {@code true} if current OS is Windows 8.1 - {@code false} otherwise.
     */
    public static boolean isWindows81() {
        return win81;
    }

    /**
     * Indicates whether current OS is Windows 2000.
     *
     * @return {@code true} if current OS is Windows 2000 - {@code false} otherwise.
     */
    public static boolean isWindows2k() {
        return win2k;
    }

    /**
     * Indicates whether current OS is Windows Server 2003.
     *
     * @return {@code true} if current OS is Windows Server 2003 - {@code false} otherwise.
     */
    public static boolean isWindows2003() {
        return win2003;
    }

    /**
     * Indicates whether current OS is Windows Server 2008.
     *
     * @return {@code true} if current OS is Windows Server 2008 - {@code false} otherwise.
     */
    public static boolean isWindows2008() {
        return win2008;
    }

    /**
     * Indicates whether current OS is Windows 95.
     *
     * @return {@code true} if current OS is Windows 95 - {@code false} otherwise.
     */
    public static boolean isWindows95() {
        return win95;
    }

    /**
     * Indicates whether current OS is Windows 98.
     *
     * @return {@code true} if current OS is Windows 98 - {@code false} otherwise.
     */
    public static boolean isWindows98() {
        return win98;
    }

    /**
     * Indicates whether current OS is Windows NT.
     *
     * @return {@code true} if current OS is Windows NT - {@code false} otherwise.
     */
    public static boolean isWindowsNt() {
        return winNt;
    }

    /**
     * Indicates that GridGain has been sufficiently tested on the current OS.
     *
     * @return {@code true} if current OS was sufficiently tested - {@code false} otherwise.
     */
    public static boolean isSufficientlyTestedOs() {
        return
            win7 ||
                win8 ||
                win81 ||
                winXp ||
                winVista ||
                mac ||
                linux ||
                solaris;
    }

    /**
     * Indicates whether current OS is Windows XP.
     *
     * @return {@code true} if current OS is Windows XP- {@code false} otherwise.
     */
    public static boolean isWindowsXp() {
        return winXp;
    }

    /**
     * Gets JVM specification name.
     *
     * @return JVM specification name.
     */
    public static String jvmSpec() {
        return jvmSpecName;
    }

    /**
     * Gets JVM implementation version.
     *
     * @return JVM implementation version.
     */
    public static String jvmVersion() {
        return jvmImplVer;
    }

    /**
     * Gets JVM implementation vendor.
     *
     * @return JVM implementation vendor.
     */
    public static String jvmVendor() {
        return jvmImplVendor;
    }

    /**
     * Gets JVM implementation name.
     *
     * @return JVM implementation name.
     */
    public static String jvmName() {
        return jvmImplName;
    }

    /**
     * Compare java implementation version
     *
     * @param v1 - java implementation version
     * @param v2 - java implementation version
     * @return the value {@code 0} if {@code v1 == v2};
     *         a value less than {@code 0} if {@code v1 < v2}; and
     *         a value greater than {@code 0} if {@code v1 > v2}
     */
    public static int compareVersionNumbers(@Nullable String v1, @Nullable String v2) {
        if (v1 == null && v2 == null)
            return 0;

        if (v1 == null)
            return -1;

        if (v2 == null)
            return 1;

        String[] part1 = v1.split("[\\.\\_\\-]");
        String[] part2 = v2.split("[\\.\\_\\-]");
        int idx = 0;

        for (; idx < part1.length && idx < part2.length; idx++) {
            String p1 = part1[idx];
            String p2 = part2[idx];

            int cmp = (p1.matches("\\d+") && p2.matches("\\d+"))
                        ? Integer.valueOf(p1).compareTo(Integer.valueOf(p2)) : p1.compareTo(p2);

            if (cmp != 0)
                return cmp;
        }

        if (part1.length == part2.length)
            return 0;
        else
            return part1.length > idx ? 1 : -1;
    }

    /**
     * Gets node product version based on node attributes.
     *
     * @param node Node to get version from.
     * @return Version object.
     */
    public static GridProductVersion productVersion(GridNode node) {
        String verStr = node.attribute(ATTR_BUILD_VER);
        String buildDate = node.attribute(ATTR_BUILD_DATE);

        if (buildDate != null)
            verStr += '-' + buildDate;

        return GridProductVersion.fromString(verStr);
    }

    /**
     * Compare running Java Runtime version with {@code v}
     *
     * @param v - java implementation version
     * @return {@code true} if running on Java Runtime version greater than {@code v}
     */
    public static boolean isJavaVersionAtLeast(String v) {
        return compareVersionNumbers(javaRtVer, v) >= 0;
    }

    /**
     * Gets Java Runtime name.
     *
     * @return Java Runtime name.
     */
    public static String jreName() {
        return javaRtName;
    }

    /**
     * Gets Java Runtime version.
     *
     * @return Java Runtime version.
     */
    public static String jreVersion() {
        return javaRtVer;
    }

    /**
     * Indicates whether HotSpot VM is used.
     *
     * @return {@code true} if current JVM implementation is a Sun HotSpot VM, {@code false} otherwise.
     */
    public static boolean isHotSpot() {
        return jvmImplName.contains("Java HotSpot(TM)");
    }

    /**
     * Sets thread context class loader to the given loader, executes the closure, and then
     * resets thread context class loader to its initial value.
     *
     * @param ldr Class loader to run the closure under.
     * @param c Callable to run.
     * @param <R> Return type.
     * @return Return value.
     * @throws GridException If call failed.
     */
    @Nullable public static <R> R wrapThreadLoader(ClassLoader ldr, Callable<R> c) throws GridException {
        Thread curThread = Thread.currentThread();

        // Get original context class loader.
        ClassLoader ctxLdr = curThread.getContextClassLoader();

        //noinspection CatchGenericClass
        try {
            curThread.setContextClassLoader(ldr);

            return c.call();
        }
        catch (GridException | RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new GridException(e);
        }
        finally {
            // Set the original class loader back.
            curThread.setContextClassLoader(ctxLdr);
        }
    }

    /**
     * Sets thread context class loader to the given loader, executes the closure, and then
     * resets thread context class loader to its initial value.
     *
     * @param ldr Class loader to run the closure under.
     * @param c Closure to run.
     * @param <R> Return type.
     * @return Return value.
     */
    @Nullable public static <R> R wrapThreadLoader(ClassLoader ldr, GridOutClosure<R> c) {
        Thread curThread = Thread.currentThread();

        // Get original context class loader.
        ClassLoader ctxLdr = curThread.getContextClassLoader();

        try {
            curThread.setContextClassLoader(ldr);

            return c.apply();
        }
        finally {
            // Set the original class loader back.
            curThread.setContextClassLoader(ctxLdr);
        }
    }

    /**
     * Sets thread context class loader to the given loader, executes the closure, and then
     * resets thread context class loader to its initial value.
     *
     * @param ldr Class loader to run the closure under.
     * @param c Closure to run.
     */
    public static void wrapThreadLoader(ClassLoader ldr, Runnable c) {
        Thread curThread = Thread.currentThread();

        // Get original context class loader.
        ClassLoader ctxLdr = curThread.getContextClassLoader();

        try {
            curThread.setContextClassLoader(ldr);

            c.run();
        }
        finally {
            // Set the original class loader back.
            curThread.setContextClassLoader(ctxLdr);
        }
    }

    /**
     * Short node representation.
     *
     * @param n Grid node.
     * @return Short string representing the node.
     */
    public static String toShortString(GridNode n) {
        return "GridNode [id=" + n.id() + ", order=" + n.order() + ", addr=" + n.addresses() +
            ", daemon=" + n.isDaemon() + ']';
    }

    /**
     * Short node representation.
     *
     * @param ns Grid nodes.
     * @return Short string representing the node.
     */
    public static String toShortString(Collection<? extends GridNode> ns) {
        SB sb = new SB("Grid nodes [cnt=" + ns.size());

        for (GridNode n : ns)
            sb.a(", ").a(toShortString(n));

        return sb.a(']').toString();
    }

    /**
     * Converts collection of integers into array.
     *
     * @param c Collection of integers.
     * @return Integer array.
     */
    public static int[] toIntArray(@Nullable Collection<Integer> c) {
        if (c == null || c.isEmpty())
            return EMPTY_INTS;

        int[] arr = new int[c.size()];

        int idx = 0;

        for (Integer i : c)
            arr[idx++] = i;

        return arr;
    }

    /**
     * Converts array of integers into list.
     *
     * @param arr Array of integers.
     * @param p Optional predicate array.
     * @return List of integers.
     */
    public static List<Integer> toIntList(@Nullable int[] arr, GridPredicate<Integer>... p) {
        if (arr == null || arr.length == 0)
            return Collections.emptyList();

        List<Integer> ret = new ArrayList<>(arr.length);

        if (F.isEmpty(p))
            for (int i : arr)
                ret.add(i);
        else {
            for (int i : arr)
                if (F.isAll(i, p))
                    ret.add(i);
        }

        return ret;
    }

    /**
     * Converts collection of integers into array.
     *
     * @param c Collection of integers.
     * @return Integer array.
     */
    public static long[] toLongArray(@Nullable Collection<Long> c) {
        if (c == null || c.isEmpty())
            return EMPTY_LONGS;

        long[] arr = new long[c.size()];

        int idx = 0;

        for (Long l : c)
            arr[idx++] = l;

        return arr;
    }

    /**
     * Converts array of longs into list.
     *
     * @param arr Array of longs.
     * @return List of longs.
     */
    public static List<Long> toLongList(@Nullable long[] arr) {
        if (arr == null || arr.length == 0)
            return Collections.emptyList();

        List<Long> ret = new ArrayList<>(arr.length);

        for (long l : arr)
            ret.add(l);

        return ret;
    }

    /**
     * Copies all elements from collection to array and asserts that
     * array is big enough to hold the collection. This method should
     * always be preferred to {@link Collection#toArray(Object[])}
     * method.
     *
     * @param c Collection to convert to array.
     * @param arr Array to populate.
     * @param <T> Element type.
     * @return Passed in array.
     */
    @SuppressWarnings({"MismatchedReadAndWriteOfArray"})
    public static <T> T[] toArray(Collection<? extends T> c, T[] arr) {
        T[] a = c.toArray(arr);

        assert a == arr;

        return arr;
    }

    /**
     * Returns array which is the union of two arrays
     * (array of elements contained in any of provided arrays).
     * <p/>
     * Note: arrays must be increasing.
     *
     * @param a First array.
     * @param aLen Length of prefix {@code a}.
     * @param b Second array.
     * @param bLen Length of prefix {@code b}.
     * @return Increasing array which is union of {@code a} and {@code b}.
     */
    @SuppressWarnings("IfMayBeConditional")
    public static int[] unique(int[] a, int aLen, int[] b, int bLen) {
        assert a != null;
        assert b != null;
        assert isIncreasingArray(a, aLen);
        assert isIncreasingArray(b, bLen);

        int[] res = new int[aLen + bLen];
        int resLen = 0;

        int i = 0;
        int j = 0;

        while (i < aLen && j < bLen) {
            if (a[i] == b[j])
                i++;
            else if (a[i] < b[j])
                res[resLen++] = a[i++];
            else
                res[resLen++] = b[j++];
        }

        while (i < aLen)
            res[resLen++] = a[i++];

        while (j < bLen)
            res[resLen++] = b[j++];

        return copyIfExceeded(res, resLen);
    }

    /**
     * Returns array which is the difference between two arrays
     * (array of elements contained in first array but not contained in second).
     * <p/>
     * Note: arrays must be increasing.
     *
     * @param a First array.
     * @param aLen Length of prefix {@code a}.
     * @param b Second array.
     * @param bLen Length of prefix {@code b}.
     * @return Increasing array which is difference between {@code a} and {@code b}.
     */
    @SuppressWarnings("IfMayBeConditional")
    public static int[] difference(int[] a, int aLen, int[] b, int bLen) {
        assert a != null;
        assert b != null;
        assert isIncreasingArray(a, aLen);
        assert isIncreasingArray(b, bLen);

        int[] res = new int[aLen];
        int resLen = 0;

        int i = 0;
        int j = 0;

        while (i < aLen && j < bLen) {
            if (a[i] == b[j])
                i++;
            else if (a[i] < b[j])
                res[resLen++] = a[i++];
            else
                j++;
        }

        while (i < aLen)
            res[resLen++] = a[i++];

        return copyIfExceeded(res, resLen);
    }

    /**
     * Checks if array prefix increases.
     *
     * @param arr Array.
     * @param len Prefix length.
     * @return {@code True} if {@code arr} from 0 to ({@code len} - 1) increases.
     */
    public static boolean isIncreasingArray(int[] arr, int len) {
        assert arr != null;
        assert 0 <= len && len <= arr.length;

        if (arr.length == 0)
            return true;

        for (int i = 1; i < len; i++) {
            if (arr[i - 1] >= arr[i])
                return false;
        }

        return true;
    }

    /**
     * Checks if array prefix do not decreases.
     *
     * @param arr Array.
     * @param len Prefix length.
     * @return {@code True} if {@code arr} from 0 to ({@code len} - 1) do not decreases.
     */
    public static boolean isNonDecreasingArray(int[] arr, int len) {
        assert arr != null;
        assert 0 <= len && len <= arr.length;

        if (arr.length == 0)
            return true;

        for (int i = 1; i < len; i++) {
            if (arr[i - 1] > arr[i])
                return false;
        }

        return true;
    }

    /**
     * Copies array only if array length greater than needed length.
     *
     * @param arr Array.
     * @param len Prefix length.
     * @return Old array if length of {@code arr} is equals to {@code len},
     *      otherwise copy of array.
     */
    public static int[] copyIfExceeded(int[] arr, int len) {
        assert arr != null;
        assert 0 <= len && len <= arr.length;

        return len == arr.length ? arr : Arrays.copyOf(arr, len);
    }

    /**
     *
     * @param t Tokenizer.
     * @param str Input string.
     * @param date Date.
     * @return Next token.
     * @throws GridException Thrown in case of any errors.
     */
    private static boolean checkNextToken(StringTokenizer t, String str, String date) throws GridException {
        try {
            if (t.nextToken().equals(str))
                return true;
            else
                throw new GridException("Invalid date format: " + date);
        }
        catch (NoSuchElementException ignored) {
            return false;
        }
    }

    /**
     *
     * @param str ISO date.
     * @return Calendar instance.
     * @throws GridException Thrown in case of any errors.
     */
    public static Calendar parseIsoDate(String str) throws GridException {
        StringTokenizer t = new StringTokenizer(str, "+-:.TZ", true);

        Calendar cal = Calendar.getInstance();
        cal.clear();

        try {
            if (t.hasMoreTokens())
                cal.set(Calendar.YEAR, Integer.parseInt(t.nextToken()));
            else
                return cal;

            if (checkNextToken(t, "-", str) && t.hasMoreTokens())
                cal.set(Calendar.MONTH, Integer.parseInt(t.nextToken()) - 1);
            else
                return cal;

            if (checkNextToken(t, "-", str) && t.hasMoreTokens())
                cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(t.nextToken()));
            else
                return cal;

            if (checkNextToken(t, "T", str) && t.hasMoreTokens())
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(t.nextToken()));
            else {
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                return cal;
            }

            if (checkNextToken(t, ":", str) && t.hasMoreTokens())
                cal.set(Calendar.MINUTE, Integer.parseInt(t.nextToken()));
            else {
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                return cal;
            }

            if (!t.hasMoreTokens())
                return cal;

            String tok = t.nextToken();

            if (":".equals(tok)) { // Seconds.
                if (t.hasMoreTokens()) {
                    cal.set(Calendar.SECOND, Integer.parseInt(t.nextToken()));

                    if (!t.hasMoreTokens())
                        return cal;

                    tok = t.nextToken();

                    if (".".equals(tok)) {
                        String nt = t.nextToken();

                        while (nt.length() < 3)
                            nt += "0";

                        nt = nt.substring(0, 3); // Cut trailing chars.

                        cal.set(Calendar.MILLISECOND, Integer.parseInt(nt));

                        if (!t.hasMoreTokens())
                            return cal;

                        tok = t.nextToken();
                    }
                    else
                        cal.set(Calendar.MILLISECOND, 0);
                }
                else
                    throw new GridException("Invalid date format: " + str);
            }
            else {
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            }

            if (!"Z".equals(tok)) {
                if (!"+".equals(tok) && !"-".equals(tok))
                    throw new GridException("Invalid date format: " + str);

                boolean plus = "+".equals(tok);

                if (!t.hasMoreTokens())
                    throw new GridException("Invalid date format: " + str);

                tok = t.nextToken();

                int tzHour;
                int tzMin;

                if (tok.length() == 4) {
                    tzHour = Integer.parseInt(tok.substring(0, 2));
                    tzMin = Integer.parseInt(tok.substring(2, 4));
                }
                else {
                    tzHour = Integer.parseInt(tok);

                    if (checkNextToken(t, ":", str) && t.hasMoreTokens())
                        tzMin = Integer.parseInt(t.nextToken());
                    else
                        throw new GridException("Invalid date format: " + str);
                }

                if (plus)
                    cal.set(Calendar.ZONE_OFFSET, (tzHour * 60 + tzMin) * 60 * 1000);
                else
                    cal.set(Calendar.ZONE_OFFSET, -(tzHour * 60 + tzMin) * 60 * 1000);
            }
            else
                cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        catch (NumberFormatException ex) {
            throw new GridException("Invalid date format: " + str, ex);
        }

        return cal;
    }

    /**
     * Adds values to collection and returns the same collection to allow chaining.
     *
     * @param c Collection to add values to.
     * @param vals Values.
     * @param <V> Value type.
     * @return Passed in collection.
     */
    public static <V, C extends Collection<? super V>> C addAll(C c, V... vals) {
        Collections.addAll(c, vals);

        return c;
    }

    /**
     * Adds values to collection and returns the same collection to allow chaining.
     *
     * @param m Map to add entries to.
     * @param entries Entries.
     * @param <K> Key type.
     * @param <V> Value type.
     * @param <M> Map type.
     * @return Passed in collection.
     */
    public static <K, V, M extends Map<K, V>> M addAll(M m, Map.Entry<K, V>... entries) {
        for (Map.Entry<K, V> e : entries)
            m.put(e.getKey(), e.getValue());

        return m;
    }

    /**
     * Adds values to collection and returns the same collection to allow chaining.
     *
     * @param m Map to add entries to.
     * @param entries Entries.
     * @param <K> Key type.
     * @param <V> Value type.
     * @param <M> Map type.
     * @return Passed in collection.
     */
    public static <K, V, M extends Map<K, V>> M addAll(M m, GridBiTuple<K, V>... entries) {
        for (GridBiTuple<K, V> t : entries)
            m.put(t.get1(), t.get2());

        return m;
    }

    /**
     * Utility method creating {@link JMException} with given cause.
     *
     * @param e Cause exception.
     * @return Newly created {@link JMException}.
     */
    public static JMException jmException(Throwable e) {
        JMException x = new JMException();

        x.initCause(e);

        return x;
    }

    /**
     * Unwraps closure exceptions.
     *
     * @param t Exception.
     * @return Unwrapped exception.
     */
    public static Exception unwrap(Throwable t) {
        assert t != null;

        while (true) {
            if (t instanceof Error)
                throw (Error)t;

            if (t instanceof GridClosureException) {
                t = ((GridClosureException)t).unwrap();

                continue;
            }

            return (Exception)t;
        }
    }

    /**
     * Casts this throwable as {@link GridException}. Creates wrapping
     * {@link GridException}, if needed.
     *
     * @param t Throwable to cast.
     * @return Grid exception.
     */
    public static GridException cast(Throwable t) {
        assert t != null;

        while (true) {
            if (t instanceof Error)
                throw (Error)t;

            if (t instanceof GridClosureException) {
                t = ((GridClosureException)t).unwrap();

                continue;
            }

            if (t instanceof GridException)
                return (GridException)t;

            if (!(t instanceof GridRuntimeException) || t.getCause() == null)
                return new GridException(t);

            assert t.getCause() != null; // ...and it is GridRuntimeException.

            t = t.getCause();
        }
    }

    /**
     * Parses passed string with specified date.
     *
     * @param src String to parse.
     * @param ptrn Pattern.
     * @return Parsed date.
     * @throws java.text.ParseException If exception occurs while parsing.
     */
    public static Date parse(String src, String ptrn) throws java.text.ParseException {
        java.text.DateFormat format = new java.text.SimpleDateFormat(ptrn);

        return format.parse(src);
    }

    /**
     * Checks if class loader is an internal P2P class loader.
     *
     * @param o Object to check.
     * @return {@code True} if P2P class loader.
     */
    public static boolean p2pLoader(Object o) {
        return o != null && p2pLoader(o.getClass().getClassLoader());
    }

    /**
     * Checks if class loader is an internal P2P class loader.
     *
     * @param ldr Class loader to check.
     * @return {@code True} if P2P class loader.
     */
    public static boolean p2pLoader(ClassLoader ldr) {
        return ldr instanceof GridDeploymentInfo;
    }

    /**
     * Formats passed date with specified pattern.
     *
     * @param date Date to format.
     * @param ptrn Pattern.
     * @return Formatted date.
     */
    public static String format(Date date, String ptrn) {
        java.text.DateFormat format = new java.text.SimpleDateFormat(ptrn);

        return format.format(date);
    }

    /**
     * @param ctx Kernal context.
     * @return Closure that converts node ID to a node.
     */
    public static GridClosure<UUID, GridNode> id2Node(final GridKernalContext ctx) {
        assert ctx != null;

        return new C1<UUID, GridNode>() {
            @Nullable @Override public GridNode apply(UUID id) {
                return ctx.discovery().node(id);
            }
        };
    }

    /**
     * Dumps stack for given thread.
     *
     * @param t Thread to dump stack for.
     *
     * @deprecated Calls to this method should never be committed to master.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public static void dumpStack(Thread t) {
        dumpStack(t, System.err);
    }

    /**
     * Dumps stack for given thread.
     *
     * @param t Thread to dump stack for.
     * @param s {@code PrintStream} to use for output.
     *
     * @deprecated Calls to this method should never be committed to master.
     */
    @SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
    @Deprecated
    public static void dumpStack(Thread t, PrintStream s) {
        synchronized (s) {
            s.println("Dumping stack trace for thread: " + t);

            for (StackTraceElement trace : t.getStackTrace())
                s.println("\tat " + trace);
        }
    }

    /**
     * Checks if object is a primitive array.
     *
     * @param obj Object to check.
     * @return {@code True} if Object is primitive array.
     */
    public static boolean isPrimitiveArray(Object obj) {
        Class<?> cls = obj.getClass();

        return cls.isArray() && (
            cls.equals(byte[].class) ||
                cls.equals(short[].class) ||
                cls.equals(char[].class) ||
                cls.equals(int[].class) ||
                cls.equals(long[].class) ||
                cls.equals(float[].class) ||
                cls.equals(double[].class) ||
                cls.equals(boolean[].class));
    }

    /**
     * @param cls Class.
     * @return {@code True} if given class represents a primitive or a primitive wrapper class.
     *
     */
    public static boolean isPrimitiveOrWrapper(Class<?> cls) {
        return cls.isPrimitive() ||
            Boolean.class.equals(cls) ||
            Byte.class.equals(cls) ||
            Character.class.equals(cls) ||
            Short.class.equals(cls) ||
            Integer.class.equals(cls) ||
            Long.class.equals(cls) ||
            Float.class.equals(cls) ||
            Double.class.equals(cls) ||
            Void.class.equals(cls);
    }

    /**
     * Awaits for condition.
     *
     * @param cond Condition to await for.
     * @throws GridInterruptedException Wrapped {@link InterruptedException}
     */
    public static void await(Condition cond) throws GridInterruptedException {
        try {
            cond.await();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new GridInterruptedException(e);
        }
    }

    /**
     * Awaits for condition.
     *
     * @param cond Condition to await for.
     * @param time The maximum time to wait,
     * @param unit The unit of the {@code time} argument.
     * @return {@code false} if the waiting time detectably elapsed before return from the method, else {@code true}
     * @throws GridInterruptedException Wrapped {@link InterruptedException}
     */
    public static boolean await(Condition cond, long time, TimeUnit unit) throws GridInterruptedException {
        try {
            return cond.await(time, unit);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new GridInterruptedException(e);
        }
    }

    /**
     * Awaits for the latch.
     *
     * @param latch Latch to wait for.
     * @throws GridInterruptedException Wrapped {@link InterruptedException}.
     */
    public static void await(CountDownLatch latch) throws GridInterruptedException {
        try {
            if (latch.getCount() > 0)
                latch.await();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new GridInterruptedException(e);
        }
    }

    /**
     * Awaits for the latch.
     *
     * @param latch Latch to wait for.
     * @param timeout Maximum time to wait.
     * @param unit Time unit for timeout.
     * @return {@code True} if the count reached zero and {@code false}
     *      if the waiting time elapsed before the count reached zero.
     * @throws GridInterruptedException Wrapped {@link InterruptedException}.
     */
    public static boolean await(CountDownLatch latch, long timeout, TimeUnit unit)
        throws GridInterruptedException {
        try {
            return latch.await(timeout, unit);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new GridInterruptedException(e);
        }
    }

    /**
     * Awaits for the latch until it is counted down,
     * ignoring interruptions.
     * <p>
     * If calling thread was interrupted, interrupted status will be
     * recovered prior to return.
     *
     * @param latch Latch to wait for.
     */
    public static void awaitQuiet(CountDownLatch latch) {
        boolean interrupted = false;

        while (true) {
            try {
                latch.await();

                break;
            }
            catch (InterruptedException ignored) {
                interrupted = true;
            }
        }

        if (interrupted)
            Thread.currentThread().interrupt();
    }

    /**
     * Awaits for the barrier ignoring interruptions.
     * <p>
     * If calling thread was interrupted, interrupted status will be recovered prior to return. If the barrier is
     * already broken, return immediately without throwing any exceptions.
     *
     * @param barrier Barrier to wait for.
     */
    public static void awaitQuiet(CyclicBarrier barrier) {
        boolean interrupted = false;

        while (true) {
            try {
                barrier.await();

                break;
            }
            catch (InterruptedException ignored) {
                interrupted = true;
            }
            catch (BrokenBarrierException ignored) {
                break;
            }
        }

        if (interrupted)
            Thread.currentThread().interrupt();
    }

    /**
     * Sleeps for given number of milliseconds.
     *
     * @param ms Time to sleep.
     * @throws GridInterruptedException Wrapped {@link InterruptedException}.
     */
    public static void sleep(long ms) throws GridInterruptedException {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new GridInterruptedException(e);
        }
    }

    /**
     * Joins worker.
     *
     * @param w Worker.
     * @throws GridInterruptedException Wrapped {@link InterruptedException}.
     */
    public static void join(GridWorker w) throws GridInterruptedException {
        try {
            if (w != null)
                w.join();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new GridInterruptedException(e);
        }
    }

    /**
     * Gets result from the given future with right exception handling.
     *
     * @param fut Future.
     * @return Future result.
     * @throws GridException If failed.
     */
    public static <T> T get(Future<T> fut) throws GridException {
        try {
            return fut.get();
        }
        catch (ExecutionException e) {
            throw new GridException(e.getCause());
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new GridInterruptedException(e);
        }
        catch (CancellationException e) {
            throw new GridException(e);
        }
    }

    /**
     * Joins thread.
     *
     * @param t Thread.
     * @throws GridInterruptedException Wrapped {@link InterruptedException}.
     */
    public static void join(Thread t) throws GridInterruptedException {
        try {
            t.join();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new GridInterruptedException(e);
        }
    }

    /**
     * Acquires a permit from provided semaphore.
     *
     * @param sem Semaphore.
     * @throws GridInterruptedException Wrapped {@link InterruptedException}.
     */
    public static void acquire(Semaphore sem) throws GridInterruptedException {
        try {
            sem.acquire();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new GridInterruptedException(e);
        }
    }

    /**
     * Gets cache attributes for the node.
     *
     * @param n Node to get cache attributes for.
     * @return Array of cache attributes for the node.
     */
    public static GridCacheAttributes[] cacheAttributes(GridNode n) {
        return n.attribute(ATTR_CACHE);
    }

    /**
     * Gets cache attributes from the given node for the given cache name.
     *
     * @param n Node.
     * @param cacheName Cache name.
     * @return Attributes.
     */
    @Nullable public static GridCacheAttributes cacheAttributes(GridNode n, @Nullable String cacheName) {
        for (GridCacheAttributes a : cacheAttributes(n)) {
            if (F.eq(a.cacheName(), cacheName))
                return a;
        }

        return null;
    }

    /**
     * Gets portable enabled flag from the given node for the given cache name.
     *
     * @param n Node.
     * @param cacheName Cache name.
     * @return Portable enabled flag.
     */
    @Nullable public static Boolean portableEnabled(GridNode n, @Nullable String cacheName) {
        Map<String, Boolean> map = n.attribute(ATTR_CACHE_PORTABLE);

        return map == null ? null : map.get(cacheName);
    }

    /**
     * Gets view on all cache names started on the node.
     *
     * @param n Node to get cache names for.
     * @return Cache names for the node.
     */
    public static Collection<String> cacheNames(GridNode n) {
        return F.viewReadOnly(
            F.asList(n.<GridCacheAttributes[]>attribute(ATTR_CACHE)),
            new C1<GridCacheAttributes, String>() {
                @Override public String apply(GridCacheAttributes attrs) {
                    return attrs.cacheName();
                }
            });
    }

    /**
     * Checks if given node has specified cache started.
     *
     * @param n Node to check.
     * @param cacheName Cache name to check.
     * @return {@code True} if given node has specified cache started.
     */
    public static boolean hasCache(GridNode n, @Nullable String cacheName) {
        assert n != null;

        GridCacheAttributes[] caches = n.attribute(ATTR_CACHE);

        if (caches != null)
            for (GridCacheAttributes attrs : caches)
                if (F.eq(cacheName, attrs.cacheName()))
                    return true;

        return false;
    }

    /**
     * Checks if given node has specified streamer started.
     *
     * @param n Node to check.
     * @param streamerName Streamer name to check.
     * @return {@code True} if given node has specified streamer started.
     */
    public static boolean hasStreamer(GridNode n, @Nullable String streamerName) {
        assert n != null;

        GridStreamerAttributes[] attrs = n.attribute(ATTR_STREAMER);

        if (attrs != null) {
            for (GridStreamerAttributes attr : attrs) {
                if (F.eq(streamerName, attr.name()))
                    return true;
            }
        }

        return false;
    }

    /**
     * Gets cache mode or a cache on given node or {@code null} if cache is not
     * present on given node.
     *
     * @param n Node to check.
     * @param cacheName Cache to check.
     * @return Cache mode or {@code null} if cache is not found.
     */
    @Nullable public static GridCacheMode cacheMode(GridNode n, String cacheName) {
        GridCacheAttributes[] caches = n.attribute(ATTR_CACHE);

        if (caches != null)
            for (GridCacheAttributes attrs : caches)
                if (F.eq(cacheName, attrs.cacheName()))
                    return attrs.cacheMode();

        return null;
    }

    /**
     * Gets cache mode or a cache on given node or {@code null} if cache is not
     * present on given node.
     *
     * @param n Node to check.
     * @param cacheName Cache to check.
     * @return Cache mode or {@code null} if cache is not found.
     */
    @Nullable public static GridCacheAtomicityMode atomicityMode(GridNode n, String cacheName) {
        GridCacheAttributes[] caches = n.attribute(ATTR_CACHE);

        if (caches != null)
            for (GridCacheAttributes attrs : caches)
                if (F.eq(cacheName, attrs.cacheName()))
                    return attrs.atomicityMode();

        return null;
    }

    /**
     * Checks if given node has near cache enabled for the specified
     * partitioned cache.
     *
     * @param n Node.
     * @param cacheName Cache name.
     * @return {@code true} if given node has near cache enabled for the
     *      specified partitioned cache.
     */
    public static boolean hasNearCache(GridNode n, String cacheName) {
        GridCacheAttributes[] caches = n.attribute(ATTR_CACHE);

        if (caches != null)
            for (GridCacheAttributes attrs : caches)
                if (F.eq(cacheName, attrs.cacheName()))
                    return attrs.nearCacheEnabled();

        return false;
    }

    /**
     * Adds listener to asynchronously log errors.
     *
     * @param f Future to listen to.
     * @param log Logger.
     */
    public static void asyncLogError(GridFuture<?> f, final GridLogger log) {
        if (f != null)
            f.listenAsync(new CI1<GridFuture<?>>() {
                @Override public void apply(GridFuture<?> f) {
                    try {
                        f.get();
                    }
                    catch (GridException e) {
                        U.error(log, "Failed to execute future: " + f, e);
                    }
                }
            });
    }

    /**
     * Converts collection of nodes to collection of node IDs.
     *
     * @param nodes Nodes.
     * @return Node IDs.
     */
    public static Collection<UUID> nodeIds(@Nullable Collection<? extends GridNode> nodes) {
        return F.viewReadOnly(nodes, F.node2id());
    }

    /**
     * Converts collection of Grid instances to collection of node IDs.
     *
     * @param grids Grids.
     * @return Node IDs.
     */
    public static Collection<UUID> gridIds(@Nullable Collection<? extends Grid> grids) {
        return F.viewReadOnly(grids, new C1<Grid, UUID>() {
            @Override public UUID apply(Grid g) {
                return g.localNode().id();
            }
        });
    }

    /**
     * Converts collection of Grid instances to collection of grid names.
     *
     * @param grids Grids.
     * @return Grid names.
     */
    public static Collection<String> grids2names(@Nullable Collection<? extends Grid> grids) {
        return F.viewReadOnly(grids, new C1<Grid, String>() {
            @Override public String apply(Grid g) {
                return g.name();
            }
        });
    }

    /**
     * Converts collection of grid nodes to collection of grid names.
     *
     * @param nodes Nodes.
     * @return Grid names.
     */
    public static Collection<String> nodes2names(@Nullable Collection<? extends GridNode> nodes) {
        return F.viewReadOnly(nodes, new C1<GridNode, String>() {
            @Override public String apply(GridNode n) {
                return G.grid(n.id()).name();
            }
        });
    }

    /**
     * Adds cause to the end of cause chain.
     *
     * @param e Error to add cause to.
     * @param cause Cause to add.
     * @param log Logger to log failure when cause can not be added.
     * @return {@code True} if cause was added.
     */
    public static boolean addLastCause(@Nullable Throwable e, @Nullable Throwable cause, GridLogger log) {
        if (e == null || cause == null)
            return false;

        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t == cause)
                return false;

            if (t.getCause() == null || t.getCause() == t) {
                try {
                    t.initCause(cause);
                }
                catch (IllegalStateException ignored) {
                    error(log, "Failed to add cause to the end of cause chain (cause is printed here but will " +
                        "not be propagated to callee): " + e,
                        "Failed to add cause to the end of cause chain: " + e, cause);
                }

                return true;
            }
        }

        return false;
    }

    /**
     * @return {@code line.separator} system property.
     */
    public static String nl() {
        return NL;
    }

    /**
     * Initializes logger into/from log reference passed in.
     *
     * @param ctx Context.
     * @param logRef Log reference.
     * @param obj Object to get logger for.
     * @return Logger for the object.
     */
    public static GridLogger logger(GridKernalContext ctx, AtomicReference<GridLogger> logRef, Object obj) {
        GridLogger log = logRef.get();

        if (log == null) {
            logRef.compareAndSet(null, ctx.log(obj.getClass()));

            log = logRef.get();
        }

        return log;
    }

    /**
     * Initializes logger into/from log reference passed in.
     *
     * @param ctx Context.
     * @param logRef Log reference.
     * @param cls Class to get logger for.
     * @return Logger for the object.
     */
    public static GridLogger logger(GridKernalContext ctx, AtomicReference<GridLogger> logRef, Class<?> cls) {
        GridLogger log = logRef.get();

        if (log == null) {
            logRef.compareAndSet(null, ctx.log(cls));

            log = logRef.get();
        }

        return log;
    }

    /**
     * @param hash Hash code of the object to put.
     * @param concurLvl Concurrency level.
     * @return Segment index.
     */
    public static int concurrentMapSegment(int hash, int concurLvl) {
        hash += (hash <<  15) ^ 0xffffcd7d;
        hash ^= (hash >>> 10);
        hash += (hash <<   3);
        hash ^= (hash >>>  6);
        hash += (hash <<   2) + (hash << 14);

        int shift = 0;
        int size = 1;

        while (size < concurLvl) {
            ++shift;
            size <<= 1;
        }

        int segmentShift = 32 - shift;
        int segmentMask = size - 1;

        return (hash >>> segmentShift) & segmentMask;
    }

    /**
     * @param map Map.
     */
    public static <K, V> void printConcurrentHashMapInfo(ConcurrentHashMap<K, V> map) {
        assert map != null;

        Object[] segs = field(map, "segments");

        X.println("Concurrent map stats [identityHash= " + System.identityHashCode(map) +
            ", segsCnt=" + segs.length + ']');

        int emptySegsCnt = 0;

        int totalCollisions = 0;

        for (int i = 0; i < segs.length; i++) {
            int segCnt = GridUtils.<Integer>field(segs[i], "count");

            if (segCnt == 0) {
                emptySegsCnt++;

                continue;
            }

            Object[] tab = field(segs[i], "table");

            int tabLen = tab.length;

            X.println("    Segment-" + i + " [count=" + segCnt + ", len=" + tabLen + ']');

            // Group buckets by entries count.
            Map<Integer, Integer> bucketsStats = new TreeMap<>();

            for (Object entry : tab) {
                int cnt = 0;

                while (entry != null) {
                    cnt++;

                    entry = field(entry, "next");
                }

                Integer bucketCnt = bucketsStats.get(cnt);

                if (bucketCnt == null)
                    bucketCnt = 0;

                bucketCnt++;

                bucketsStats.put(cnt, bucketCnt);

                if (cnt > 1)
                    totalCollisions += (cnt - 1);
            }

            for (Map.Entry<Integer, Integer> e : bucketsStats.entrySet())
                X.println("        Buckets with count " + e.getKey() + ": " + e.getValue());
        }

        X.println("    Map summary [emptySegs=" + emptySegsCnt + ", collisions=" + totalCollisions + ']');
    }

    /**
     * Gets field value.
     *
     * @param obj Object.
     * @param fieldName Field name.
     * @return Field value.
     */
    public static <T> T field(Object obj, String fieldName) {
        assert obj != null;
        assert fieldName != null;

        try {
            for (Class cls = obj.getClass(); cls != Object.class; cls = cls.getSuperclass()) {
                for (Field field : cls.getDeclaredFields()) {
                    if (field.getName().equals(fieldName)) {
                        boolean accessible = field.isAccessible();

                        field.setAccessible(true);

                        T val = (T)field.get(obj);

                        if (!accessible)
                            field.setAccessible(false);

                        return val;
                    }
                }
            }
        }
        catch (Exception e) {
            throw new GridRuntimeException("Failed to get field value [fieldName=" + fieldName + ", obj=" + obj + ']',
                e);
        }

        throw new GridRuntimeException("Failed to get field value [fieldName=" + fieldName + ", obj=" + obj + ']');
    }

    /**
     * Gets field value.
     *
     * @param cls Class.
     * @param fieldName Field name.
     * @return Field value.
     * @throws GridException If static field with given name cannot be retreived.
     */
    public static <T> T field(Class<?> cls, String fieldName) throws GridException {
        assert cls != null;
        assert fieldName != null;

        try {
            for (Class c = cls; cls != Object.class; cls = cls.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    if (field.getName().equals(fieldName)) {
                        if (!Modifier.isStatic(field.getModifiers()))
                            throw new GridException("Failed to get class field (field is not static) [cls=" +
                                cls + ", fieldName=" + fieldName + ']');

                        boolean accessible = field.isAccessible();

                        T val;

                        try {
                            field.setAccessible(true);

                            val = (T)field.get(null);
                        }
                        finally {
                            if (!accessible)
                                field.setAccessible(false);
                        }

                        return val;
                    }
                }
            }
        }
        catch (Exception e) {
            throw new GridException("Failed to get field value [fieldName=" + fieldName + ", cls=" + cls + ']',
                e);
        }

        throw new GridException("Failed to get field value (field was not found) [fieldName=" + fieldName +
            ", cls=" + cls + ']');
    }

    /**
     * Invokes method.
     *
     * @param cls Object.
     * @param obj Object.
     * @param mtdName Field name.
     * @param paramTypes Parameter types.
     * @param params Parameters.
     * @return Field value.
     * @throws GridException If static field with given name cannot be retreived.
     */
    public static <T> T invoke(@Nullable Class<?> cls, @Nullable Object obj, String mtdName,
        Class[] paramTypes, Object... params) throws GridException {
        assert cls != null || obj != null;
        assert mtdName != null;

        try {
            for (Class<?> c = cls != null ? cls : obj.getClass(); cls != Object.class; cls = cls.getSuperclass()) {
                Method mtd;

                try {
                    mtd = c.getDeclaredMethod(mtdName, paramTypes);
                }
                catch (NoSuchMethodException ignored) {
                    continue;
                }

                boolean accessible = mtd.isAccessible();

                T res;

                try {
                    mtd.setAccessible(true);

                    res = (T)mtd.invoke(obj, params);
                }
                finally {
                    if (!accessible)
                        mtd.setAccessible(false);
                }

                return res;
            }
        }
        catch (Exception e) {
            throw new GridException("Failed to invoke [mtdName=" + mtdName + ", cls=" + cls + ']',
                e);
        }

        throw new GridException("Failed to invoke (method was not found) [mtdName=" + mtdName +
            ", cls=" + cls + ']');
    }


    /**
     * Gets property value.
     *
     * @param obj Object.
     * @param propName Property name.
     * @return Field value.
     */
    public static <T> T property(Object obj, String propName) {
        assert obj != null;
        assert propName != null;

        try {
            Method m;

            try {
                m = obj.getClass().getMethod("get" + capitalFirst(propName));
            }
            catch (NoSuchMethodException ignored) {
                m = obj.getClass().getMethod("is" + capitalFirst(propName));
            }

            assert F.isEmpty(m.getParameterTypes());

            boolean accessible = m.isAccessible();

            try {
                m.setAccessible(true);

                return (T)m.invoke(obj);
            }
            finally {
                m.setAccessible(accessible);
            }
        }
        catch (Exception e) {
            throw new GridRuntimeException(
                "Failed to get property value [property=" + propName + ", obj=" + obj + ']', e);
        }
    }

    /**
     * Gets static field value.
     *
     * @param cls Class.
     * @param fieldName Field name.
     * @return Field value.
     * @throws GridException If failed.
     */
    public static <T> T staticField(Class<?> cls, String fieldName) throws GridException {
        assert cls != null;
        assert fieldName != null;

        try {
            for (Field field : cls.getDeclaredFields())
                if (field.getName().equals(fieldName)) {
                    boolean accessible = field.isAccessible();

                    if (!accessible)
                        field.setAccessible(true);

                    T val = (T)field.get(null);

                    if (!accessible)
                        field.setAccessible(false);

                    return val;
                }
        }
        catch (Exception e) {
            throw new GridException("Failed to get field value [fieldName=" + fieldName + ", cls=" + cls + ']', e);
        }

        throw new GridException("Failed to get field value [fieldName=" + fieldName + ", cls=" + cls + ']');
    }

    /**
     * Capitalizes the first character of the given string.
     *
     * @param str String.
     * @return String with capitalized first character.
     */
    private static String capitalFirst(@Nullable String str) {
        return str == null ? null :
            str.isEmpty() ? "" : Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Checks whether property is one added by Visor when node is started via remote SSH session.
     *
     * @param name Property name to check.
     * @return {@code True} if property is Visor node startup property, {@code false} otherwise.
     */
    public static boolean isVisorNodeStartProperty(String name) {
        return GG_SSH_HOST.equals(name) || GG_SSH_USER_NAME.equals(name);
    }

    /**
     * Checks whether property is one required by Visor to work correctly.
     *
     * @param name Property name to check.
     * @return {@code True} if property is required by Visor, {@code false} otherwise.
     */
    public static boolean isVisorRequiredProperty(String name) {
        return "java.version".equals(name) || "java.vm.name".equals(name) || "os.arch".equals(name) ||
            "os.name".equals(name) || "os.version".equals(name);
    }

    /**
     * Adds no-op logger to remove no-appender warning.
     *
     * @return Tuple with root log and no-op appender instances. No-op appender can be {@code null}
     *      if it did not found in classpath. Notice that in this case logging is not suppressed.
     * @throws GridException In case of failure to add no-op logger for Log4j.
     */
    public static GridBiTuple<Object, Object> addLog4jNoOpLogger() throws GridException {
        Object rootLog;
        Object nullApp;

        try {
            // Add no-op logger to remove no-appender warning.
            Class<?> logCls = Class.forName("org.apache.log4j.Logger");

            rootLog = logCls.getMethod("getRootLogger").invoke(logCls);

            try {
                nullApp = Class.forName("org.apache.log4j.varia.NullAppender").newInstance();
            }
            catch (ClassNotFoundException ignore) {
                // Can't found log4j no-op appender in classpath (for example, log4j was added through
                // log4j-over-slf4j library. No-appender warning will not be suppressed.
                return new GridBiTuple<>(rootLog, null);
            }

            Class appCls = Class.forName("org.apache.log4j.Appender");

            rootLog.getClass().getMethod("addAppender", appCls).invoke(rootLog, nullApp);
        }
        catch (Exception e) {
            throw new GridException("Failed to add no-op logger for Log4j.", e);
        }

        return new GridBiTuple<>(rootLog, nullApp);
    }

    /**
     * Removes previously added no-op logger via method {@link #addLog4jNoOpLogger}.
     *
     * @param t Tuple with root log and null appender instances.
     * @throws GridException In case of failure to remove previously added no-op logger for Log4j.
     */
    public static void removeLog4jNoOpLogger(GridBiTuple<Object, Object> t) throws GridException {
        Object rootLog = t.get1();
        Object nullApp = t.get2();

        if (nullApp == null)
            return;

        try {
            Class appenderCls = Class.forName("org.apache.log4j.Appender");

            rootLog.getClass().getMethod("removeAppender", appenderCls).invoke(rootLog, nullApp);
        }
        catch (Exception e) {
            throw new GridException("Failed to remove previously added no-op logger for Log4j.", e);
        }
    }

    /**
     * Adds no-op console handler for root java logger.
     *
     * @return Removed handlers.
     */
    public static Collection<Handler> addJavaNoOpLogger() {
        Collection<Handler> savedHnds = new ArrayList<>();

        Logger log = Logger.getLogger("");

        for (Handler h : log.getHandlers()) {
            log.removeHandler(h);

            savedHnds.add(h);
        }

        ConsoleHandler hnd = new ConsoleHandler();

        hnd.setLevel(Level.OFF);

        log.addHandler(hnd);

        return savedHnds;
    }

    /**
     * Removes previously added no-op handler for root java logger.
     *
     * @param rmvHnds Previously removed handlers.
     */
    public static void removeJavaNoOpLogger(Collection<Handler> rmvHnds) {
        Logger log = Logger.getLogger("");

        for (Handler h : log.getHandlers())
            log.removeHandler(h);

        if (!F.isEmpty(rmvHnds)) {
            for (Handler h : rmvHnds)
                log.addHandler(h);
        }
    }

    /**
     * Attaches node ID to log file name.
     *
     * @param nodeId Node ID.
     * @param fileName File name.
     * @return File name with node ID.
     */
    @SuppressWarnings("IfMayBeConditional")
    public static String nodeIdLogFileName(UUID nodeId, String fileName) {
        assert nodeId != null;
        assert fileName != null;

        fileName = GridFilenameUtils.separatorsToSystem(fileName);

        int dot = fileName.lastIndexOf('.');

        if (dot < 0 || dot == fileName.length() - 1)
            return fileName + '-' + U.id8(nodeId);
        else
            return fileName.substring(0, dot) + '-' + U.id8(nodeId) + fileName.substring(dot);
    }

    /**
     * Substitutes log directory with a custom one.
     *
     * @param dir Directory.
     * @param fileName Original path.
     * @return New path.
     */
    public static String customDirectoryLogFileName(@Nullable String dir, String fileName) {
        assert fileName != null;

        if (dir == null)
            return fileName;

        int sep = fileName.lastIndexOf(File.separator);

        return dir + (sep < 0 ? File.separator + fileName : fileName.substring(sep));
    }

    /**
     * Creates string for log output.
     *
     * @param msg Message to start string.
     * @param args Even length array where the odd elements are parameter names
     *      and even elements are parameter values.
     * @return Log message, formatted as recommended by GridGain guidelines.
     */
    public static String fl(String msg, Object... args) {
        assert args.length % 2 == 0;

        StringBuilder sb = new StringBuilder(msg);

        if (args.length > 0) {
            sb.append(" [");

            for (int i = 0; i < args.length / 2; i++) {
                sb.append(args[ i * 2]).append('=').append(args[i * 2 + 1]);
                sb.append(", ");
            }

            sb.delete(sb.length() - 2, sb.length());
            sb.append(']');
        }

        return sb.toString();
    }

    /**
     * Gets absolute value for integer. If integer is {@link Integer#MIN_VALUE}, then {@code 0} is returned.
     *
     * @param i Integer.
     * @return Absolute value.
     */
    public static int safeAbs(int i) {
        i = Math.abs(i);

        return i < 0 ? 0 : i;
    }

    /**
     * Gets wrapper class for a primitive type.
     *
     * @param cls Class. If {@code null}, method is no-op.
     * @return Wrapper class or original class if it is non-primitive.
     */
    @Nullable public static Class<?> box(@Nullable Class<?> cls) {
        if (cls == null)
            return null;

        Class<?> boxed = boxedClsMap.get(cls);

        return boxed != null ? boxed : cls;
    }

    /**
     * Gets class for provided name. Accepts primitive types names.
     *
     * @param clsName Class name.
     * @param ldr Class loader.
     * @return Class.
     * @throws ClassNotFoundException If class not found.
     */
    @Nullable public static Class<?> forName(@Nullable String clsName, @Nullable ClassLoader ldr)
        throws ClassNotFoundException {
        if (clsName == null)
            return null;

        Class<?> cls = primitiveMap.get(clsName);

        return cls != null ? cls : Class.forName(clsName, true, ldr);
    }

    /**
     * Applies a supplemental hash function to a given hashCode, which
     * defends against poor quality hash functions.  This is critical
     * because ConcurrentHashMap uses power-of-two length hash tables,
     * that otherwise encounter collisions for hashCodes that do not
     * differ in lower or upper bits.
     * <p>
     * This function has been taken from Java 8 ConcurrentHashMap with
     * slightly modifications.
     *
     * @param h Value to hash.
     * @return Hash value.
     */
    public static int hash(int h) {
        // Apply base step of MurmurHash; see http://code.google.com/p/smhasher/
        // Despite two multiplies, this is often faster than others
        // with comparable bit-spread properties.
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;

        return (h >>> 16) ^ h;
    }

    /**
     * Applies a supplemental hash function to a given hashCode, which
     * defends against poor quality hash functions.  This is critical
     * because ConcurrentHashMap uses power-of-two length hash tables,
     * that otherwise encounter collisions for hashCodes that do not
     * differ in lower or upper bits.
     * <p>
     * This function has been taken from Java 8 ConcurrentHashMap with
     * slightly modifications.
     *
     * @param key Value to hash.
     * @return Hash value.
     */
    public static int hash(Object key) {
        return hash(key.hashCode());
    }

    /**
     * Zips byte array.
     *
     * @param input Input bytes.
     * @return Zipped byte array.
     * @throws IOException If failed.
     */
    public static byte[] zipBytes(byte[] input) throws IOException {
        return zipBytes(input, 4096);
    }

    /**
     * Zips byte array.
     *
     * @param input Input bytes.
     * @param initBufSize Initial buffer size.
     * @return Zipped byte array.
     * @throws IOException If failed.
     */
    public static byte[] zipBytes(byte[] input, int initBufSize) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(initBufSize);

        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            ZipEntry entry = new ZipEntry("");

            try {
                entry.setSize(input.length);

                zos.putNextEntry(entry);

                zos.write(input);
            } finally {
                zos.closeEntry();
            }
        }

        return bos.toByteArray();
    }

    /**
     * @return PID of the current JVM or {@code -1} if it can't be determined.
     */
    public static int jvmPid() {
        // Should be something like this: 1160@mbp.local
        String name = ManagementFactory.getRuntimeMXBean().getName();

        try {
            int idx = name.indexOf('@');

            return idx > 0 ? Integer.parseInt(name.substring(0, idx)) : -1;
        }
        catch (NumberFormatException ignored) {
            return -1;
        }
    }

    /**
     * @return Input arguments passed to the JVM which does not include the arguments to the <tt>main</tt> method.
     */
    public static List<String> jvmArgs() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments();
    }

    /**
     * As long as array copying uses JVM-private API, which is not guaranteed
     * to be available on all JVM, this method should be called to ensure
     * logic could work properly.
     *
     * @return {@code True} if unsafe copying can work on the current JVM or
     *      {@code false} if it can't.
     */
    @SuppressWarnings("TypeParameterExtendsFinalClass")
    private static boolean unsafeByteArrayCopyAvailable() {
        try {
            Class<? extends Unsafe> unsafeCls = UNSAFE.getClass();

            unsafeCls.getMethod("copyMemory", Object.class, long.class, Object.class, long.class, long.class);

            return true;
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * @param src Buffer to copy from (length included).
     * @param off Offset in source buffer.
     * @param resBuf Result buffer.
     * @param resOff Result offset.
     * @param len Length.
     * @return Number of bytes overwritten in {@code bytes} array.
     */
    public static int arrayCopy(byte[] src, int off, byte[] resBuf, int resOff, int len) {
        assert resBuf.length >= resOff + len;

        if (UNSAFE_BYTE_ARR_CP)
            UNSAFE.copyMemory(src, BYTE_ARRAY_DATA_OFFSET + off, resBuf, BYTE_ARRAY_DATA_OFFSET + resOff, len);
        else
            System.arraycopy(src, off, resBuf, resOff, len);

        return resOff + len;
    }

    /**
     * Checks if exception has help URLs.
     *
     * @param msg Error message.
     * @return Formatted error message.
     */
    public static String errorMessageWithHelpUrls(String msg) {
        StringBuilder sb = msg == null ? new StringBuilder() : new StringBuilder(msg);

        sb.append("\nFor more information see:\n");

        for (String url : DFLT_HELP_LINKS)
            sb.append("    ").append(url).append("\n");

        return sb.toString();
    }

    /**
     * @param addrs Node's addresses.
     * @param port Port discovery number.
     * @return A string compatible with {@link GridNode#consistentId()} requirements.
     */
    public static String consistentId(Collection<String> addrs, int port) {
        assert !F.isEmpty(addrs);

        StringBuilder sb = new StringBuilder();

        for (String addr : addrs)
            sb.append(addr).append(',');

        sb.delete(sb.length() - 1, sb.length());

        sb.append(':').append(port);

        return sb.toString();
    }

    /**
     * @param obj Object.
     * @return {@code True} if given object has overridden equals and hashCode method.
     */
    public static boolean overridesEqualsAndHashCode(Object obj) {
        try {
            Class<?> cls = obj.getClass();

            return !Object.class.equals(cls.getMethod("equals", Object.class).getDeclaringClass()) &&
                !Object.class.equals(cls.getMethod("hashCode").getDeclaringClass());
        }
        catch (NoSuchMethodException | SecurityException ignore) {
            return true; // Ignore.
        }
    }

    /**
     * Checks if error is MAC invalid argument error which ususally requires special handling.
     *
     * @param e Exception.
     * @return {@code True} if error is invalid argument error on MAC.
     */
    public static boolean isMacInvalidArgumentError(Exception e) {
        return U.isMacOs() && e instanceof SocketException && e.getMessage() != null &&
            e.getMessage().toLowerCase().contains("invalid argument");
    }

    /**
     * Returns a first non-null value in a given array, if such is present.
     *
     * @param vals Input array.
     * @return First non-null value, or {@code null}, if array is empty or contains
     *      only nulls.
     */
    @Nullable public static <T> T firstNotNull(@Nullable T... vals) {
        if (vals == null)
            return null;

        for (T val : vals) {
            if (val != null)
                return val;
        }

        return null;
    }

    /**
     * For each object provided by the given {@link Iterable} checks if it implements
     * {@link GridLifecycleAware} interface and executes {@link GridLifecycleAware#start} method.
     *
     * @param objs Objects.
     * @throws GridException If {@link GridLifecycleAware#start} fails.
     */
    public static void startLifecycleAware(Iterable<?> objs) throws GridException {
        for (Object obj : objs) {
            if (obj instanceof GridLifecycleAware)
                ((GridLifecycleAware)obj).start();
        }
    }

    /**
     * For each object provided by the given {@link Iterable} checks if it implements
     * {@link GridLifecycleAware} interface and executes {@link GridLifecycleAware#stop} method.
     *
     * @param log Logger used to log error message in case of stop failure.
     * @param objs Object passed to GridGain configuration.
     */
    public static void stopLifecycleAware(GridLogger log, Iterable<?> objs) {
        for (Object obj : objs) {
            if (obj instanceof GridLifecycleAware) {
                try {
                    ((GridLifecycleAware)obj).stop();
                }
                catch (GridException e) {
                    U.error(log, "Failed to stop component (ignoring): " + obj, e);
                }
            }
        }
    }

    /**
     * Groups given nodes by the node's physical computer (host).
     * <p>
     * Detection of the same physical computer (host) is based on comparing set of network interface MACs.
     * If two nodes have the same set of MACs, GridGain considers these nodes running on the same
     * physical computer.
     *
     * @param nodes Nodes.
     * @return Collection of projections where each projection represents all nodes (in this projection)
     *      from a single physical computer. Result collection can be empty if this projection is empty.
     */
    public static Map<String, Collection<GridNode>> neighborhood(Iterable<GridNode> nodes) {
        Map<String, Collection<GridNode>> map = new HashMap<>();

        for (GridNode n : nodes) {
            String macs = n.attribute(ATTR_MACS);

            assert macs != null : "Missing MACs attribute: " + n;

            Collection<GridNode> neighbors = map.get(macs);

            if (neighbors == null)
                map.put(macs, neighbors = new ArrayList<>(2));

            neighbors.add(n);
        }

        return map;
    }

    /**
     * Returns tha list of resolved inet addresses. First addresses are resolved by host names,
     * if this attempt fails then the addresses are resolved by ip addresses.
     *
     * @param node Grid node.
     * @return Inet addresses for given addresses and host names.
     * @throws GridException If non of addresses can be resolved.
     */
    public static Collection<InetAddress> toInetAddresses(GridNode node) throws GridException {
        return toInetAddresses(node.addresses(), node.hostNames());
    }

    /**
     * Returns tha list of resolved inet addresses. First addresses are resolved by host names,
     * if this attempt fails then the addresses are resolved by ip addresses.
     *
     * @param addrs Addresses.
     * @param hostNames Host names.
     * @return Inet addresses for given addresses and host names.
     * @throws GridException If non of addresses can be resolved.
     */
    public static Collection<InetAddress> toInetAddresses(Collection<String> addrs,
        Collection<String> hostNames) throws GridException {
        List<InetAddress> res = new ArrayList<>(addrs.size());

        Iterator<String> hostNamesIt = hostNames.iterator();

        for (String addr : addrs) {
            String hostName = hostNamesIt.hasNext() ? hostNamesIt.next() : null;

            InetAddress inetAddr = null;

            if (!F.isEmpty(hostName)) {
                try {
                    inetAddr = InetAddress.getByName(hostName);
                }
                catch (UnknownHostException ignored) {
                }
            }

            if (inetAddr == null || inetAddr.isLoopbackAddress()) {
                try {
                    inetAddr = InetAddress.getByName(addr);
                }
                catch (UnknownHostException ignored) {
                }
            }

            if (inetAddr != null)
                res.add(inetAddr);
        }

        if (res.isEmpty())
            throw new GridException("Addresses can not be resolved [addr=" + addrs +
                ", hostNames=" + hostNames + ']');

        return F.viewListReadOnly(res, F.<InetAddress>identity());
    }

    /**
     * Returns tha list of resolved socket addresses. First addresses are resolved by host names,
     * if this attempt fails then the addresses are resolved by ip addresses.
     *
     * @param node Grid node.
     * @param port Port.
     * @return Socket addresses for given addresses and host names.
     */
    public static Collection<InetSocketAddress> toSocketAddresses(GridNode node, int port) {
        return toSocketAddresses(node.addresses(), node.hostNames(), port);
    }

    /**
     * Returns tha list of resolved socket addresses. First addresses are resolved by host names,
     * if this attempt fails then the addresses are resolved by ip addresses.
     *
     * @param addrs Addresses.
     * @param hostNames Host names.
     * @param port Port.
     * @return Socket addresses for given addresses and host names.
     */
    public static Collection<InetSocketAddress> toSocketAddresses(Collection<String> addrs,
        Collection<String> hostNames, int port) {
        List<InetSocketAddress> res = new ArrayList<>(addrs.size());

        Iterator<String> hostNamesIt = hostNames.iterator();

        for (String addr : addrs) {
            String hostName = hostNamesIt.hasNext() ? hostNamesIt.next() : null;

            if (!F.isEmpty(hostName)) {
                InetSocketAddress inetSockAddr = new InetSocketAddress(hostName, port);

                if (inetSockAddr.isUnresolved() || inetSockAddr.getAddress().isLoopbackAddress())
                    inetSockAddr = new InetSocketAddress(addr, port);

                res.add(inetSockAddr);
            }

            // Always append address because local and remote nodes may have the same hostname
            // therefore remote hostname will be always resolved to local address.
            res.add(new InetSocketAddress(addr, port));
        }

        return F.viewListReadOnly(res, F.<InetSocketAddress>identity());
    }

    /**
     * Resolves all not loopback addresses and collect results.
     *
     * @param addrRslvr Address resolver.
     * @param addrs Addresses.
     * @param port Port.
     * @return Resolved socket addresses.
     * @throws GridException If failed.
     */
    public static Collection<InetSocketAddress> resolveAddresses(
        GridAddressResolver addrRslvr,
        Iterable<String> addrs,
        int port
    ) throws GridException {
        assert addrRslvr != null;

        Collection<InetSocketAddress> extAddrs = new HashSet<>();

        for (String addr : addrs) {
            InetSocketAddress sockAddr = new InetSocketAddress(addr, port);

            if (!sockAddr.isUnresolved()) {
                try {
                    Collection<InetSocketAddress> extAddrs0 = addrRslvr.getExternalAddresses(sockAddr);

                    if (extAddrs0 != null)
                        extAddrs.addAll(extAddrs0);
                }
                catch (GridException e) {
                    throw new GridSpiException("Failed to get mapped external addresses " +
                        "[addrRslvr=" + addrRslvr + ", addr=" + addr + ']', e);
                }
            }
        }

        return extAddrs;
    }

    /**
     * Returns string representation of node addresses.
     *
     * @param node Grid node.
     * @return String representation of addresses.
     */
    public static String addressesAsString(GridNode node) {
        return addressesAsString(node.addresses(), node.hostNames());
    }

    /**
     * Returns string representation of addresses.
     *
     * @param addrs Addresses.
     * @param hostNames Host names.
     * @return String representation of addresses.
     */
    public static String addressesAsString(Collection<String> addrs, Collection<String> hostNames) {
        if (F.isEmpty(addrs))
            return "";

        if (F.isEmpty(hostNames))
            return addrs.toString();

        SB sb = new SB("[");

        Iterator<String> hostNamesIt = hostNames.iterator();

        boolean first = true;

        for (String addr : addrs) {
            if (first)
                first = false;
            else
                sb.a(", ");

            String hostName = hostNamesIt.hasNext() ? hostNamesIt.next() : null;

            sb.a(hostName != null ? hostName : "").a('/').a(addr);
        }

        sb.a(']');

        return sb.toString();
    }

    /**
     * @param userWorkDir GridGain work folder provided by user.
     * @param userGgHome GridGain home folder provided by user.
     */
    public static void setWorkDirectory(@Nullable String userWorkDir, @Nullable String userGgHome)
        throws GridException {
        String ggWork0 = ggWork;

        if (ggWork0 == null) {
            synchronized (GridUtils.class) {
                // Double check.
                ggWork0 = ggWork;

                if (ggWork0 != null)
                    return;

                File workDir;

                if (!F.isEmpty(userWorkDir))
                    workDir = new File(userWorkDir);
                else if (!F.isEmpty(GRIDGAIN_WORK_DIR))
                    workDir = new File(GRIDGAIN_WORK_DIR);
                else if (!F.isEmpty(userGgHome))
                    workDir = new File(userGgHome, "work");
                else {
                    String tmpDirPath = System.getProperty("java.io.tmpdir");

                    if (tmpDirPath == null)
                        throw new GridException("Failed to create work directory in OS temp " +
                            "(property 'java.io.tmpdir' is null).");

                    workDir = new File(tmpDirPath, "gridgain" + File.separator + "work");
                }

                if (!workDir.isAbsolute())
                    throw new GridException("Work directory path must be absolute: " + workDir);

                if (!mkdirs(workDir))
                    throw new GridException("Work directory does not exist and cannot be created: " + workDir);

                if (!workDir.canRead())
                    throw new GridException("Cannot read from work directory: " + workDir);

                if (!workDir.canWrite())
                    throw new GridException("Cannot write to work directory: " + workDir);

                ggWork = workDir.getAbsolutePath();
            }
        }
    }

    /**
     * Nullifies GridGain home directory. For test purposes only.
     */
    static void nullifyHomeDirectory() {
        ggHome = null;
    }

    /**
     * Nullifies work directory. For test purposes only.
     */
    static void nullifyWorkDirectory() {
        ggWork = null;
    }

    /**
     * Resolves work directory.
     *
     * @param path Path to resolve.
     * @param delIfExist Flag indicating whether to delete the specify directory or not.
     * @return Resolved work directory.
     * @throws GridException If failed.
     */
    public static File resolveWorkDirectory(String path, boolean delIfExist) throws GridException {
        File dir = new File(path);

        if (!dir.isAbsolute()) {
            String ggWork0 = ggWork;

            if (F.isEmpty(ggWork0))
                throw new GridException("Failed to resolve path (work directory has not been set): " + path);

            dir = new File(ggWork0, dir.getPath());
        }

        if (delIfExist && dir.exists()) {
            if (!U.delete(dir))
                throw new GridException("Failed to delete directory: " + dir);
        }

        if (!mkdirs(dir))
            throw new GridException("Directory does not exist and cannot be created: " + dir);

        if (!dir.canRead())
            throw new GridException("Cannot read from directory: " + dir);

        if (!dir.canWrite())
            throw new GridException("Cannot write to directory: " + dir);

        return dir;
    }

    /**
     * Creates {@code GridException} with the collection of suppressed exceptions.
     *
     * @param msg Message.
     * @param suppressed The collections of suppressed exceptions.
     * @return {@code GridException}.
     */
    public static GridException exceptionWithSuppressed(String msg, @Nullable Collection<Throwable> suppressed) {
        GridException e = new GridException(msg);

        if (suppressed != null) {
            for (Throwable th : suppressed)
                e.addSuppressed(th);
        }

        return e;
    }

    /**
     * Extracts full name of enclosing class from JDK8 lambda class name.
     *
     * @param clsName JDK8 lambda class name.
     * @return Full name of enclosing class for JDK8 lambda class name or
     *      {@code null} if passed in name is not related to lambda.
     */
    @Nullable public static String lambdaEnclosingClassName(String clsName) {
        int idx = clsName.indexOf("$$Lambda$");

        return idx != -1 ? clsName.substring(0, idx) : null;
    }

    /**
     * Converts an array of characters representing hexidecimal values into an
     * array of bytes of those same values. The returned array will be half the
     * length of the passed array, as it takes two characters to represent any
     * given byte. An exception is thrown if the passed char array has an odd
     * number of elements.
     *
     * @param data An array of characters containing hexidecimal digits
     * @return A byte array containing binary data decoded from
     *         the supplied char array.
     * @throws GridException Thrown if an odd number or illegal of characters is supplied.
     */
    public static byte[] decodeHex(char[] data) throws GridException {

        int len = data.length;

        if ((len & 0x01) != 0)
            throw new GridException("Odd number of characters.");

        byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;

            j++;

            f |= toDigit(data[j], j);

            j++;

            out[i] = (byte)(f & 0xFF);
        }

        return out;
    }

    /**
     * Converts a hexadecimal character to an integer.
     *
     * @param ch A character to convert to an integer digit
     * @param index The index of the character in the source
     * @return An integer
     * @throws GridException Thrown if ch is an illegal hex character
     */
    public static int toDigit(char ch, int index) throws GridException {
        int digit = Character.digit(ch, 16);

        if (digit == -1)
            throw new GridException("Illegal hexadecimal character " + ch + " at index " + index);

        return digit;
    }

    /**
     * Gets oldest node out of collection of nodes.
     *
     * @param c Collection of nodes.
     * @return Oldest node.
     */
    public static GridNode oldest(Collection<GridNode> c, @Nullable GridPredicate<GridNode> p) {
        GridNode oldest = null;

        long minOrder = Long.MAX_VALUE;

        for (GridNode n : c) {
            if ((p == null || p.apply(n)) && n.order() < minOrder) {
                oldest = n;

                minOrder = n.order();
            }
        }

        return oldest;
    }

    /**
     * Gets youngest node out of collection of nodes.
     *
     * @param c Collection of nodes.
     * @return Youngest node.
     */
    public static GridNode youngest(Collection<GridNode> c, @Nullable GridPredicate<GridNode> p) {
        GridNode youngest = null;

        long maxOrder = Long.MIN_VALUE;

        for (GridNode n : c) {
            if ((p == null || p.apply(n)) && n.order() > maxOrder) {
                youngest = n;

                maxOrder = n.order();
            }
        }

        return youngest;
    }

    /**
     * Tells whether provided type is portable.
     *
     * @param cls Class to check.
     * @return Whether type is portable.
     */
    public static boolean isPortableType(Class<?> cls) {
        assert cls != null;

        return GridPortableObject.class.isAssignableFrom(cls) ||
            PORTABLE_CLS.contains(cls) ||
            cls.isEnum() ||
            (cls.isArray() && cls.getComponentType().isEnum());
    }
    /**
     * Tells whether provided type is portable or a collection.
     *
     * @param cls Class to check.
     * @return Whether type is portable or a collection.
     */
    public static boolean isPortableOrCollectionType(Class<?> cls) {
        assert cls != null;

        return isPortableType(cls) ||
            cls == Object[].class ||
            Collection.class.isAssignableFrom(cls) ||
            Map.class.isAssignableFrom(cls) ||
            Map.Entry.class.isAssignableFrom(cls);
    }

    /**
     * @param arr Array.
     * @param off Offset.
     * @param uid UUID.
     * @return Offset.
     */
    public static long writeGridUuid(byte[] arr, long off, @Nullable GridUuid uid) {
        UNSAFE.putBoolean(arr, off++, uid != null);

        if (uid != null) {
            UNSAFE.putLong(arr, off, uid.globalId().getMostSignificantBits());

            off += 8;

            UNSAFE.putLong(arr, off, uid.globalId().getLeastSignificantBits());

            off += 8;

            UNSAFE.putLong(arr, off, uid.localId());

            off += 8;
        }

        return off;
    }

    /**
     * @param arr Array.
     * @param off Offset.
     * @return UUID.
     */
    @Nullable public static GridUuid readGridUuid(byte[] arr, long off) {
        if (UNSAFE.getBoolean(arr, off++)) {
            long most = UNSAFE.getLong(arr, off);

            off += 8;

            long least = UNSAFE.getLong(arr, off);

            off += 8;

            UUID globalId = new UUID(most, least);

            long locId = UNSAFE.getLong(arr, off);

            return new GridUuid(globalId, locId);
        }

        return null;
    }

    /**
     * @param ptr Offheap address.
     * @return UUID.
     */
    @Nullable public static GridUuid readGridUuid(long ptr) {
        if (UNSAFE.getBoolean(null, ptr++)) {
            long most = UNSAFE.getLong(ptr);

            ptr += 8;

            long least = UNSAFE.getLong(ptr);

            ptr += 8;

            UUID globalId = new UUID(most, least);

            long locId = UNSAFE.getLong(ptr);

            return new GridUuid(globalId, locId);
        }

        return null;
    }

    /**
     * @param arr Array.
     * @param off Offset.
     * @param ver Version.
     * @return Offset.
     */
    public static long writeVersion(byte[] arr, long off, GridCacheVersion ver) {
        boolean verEx = ver instanceof GridCacheVersionEx;

        UNSAFE.putBoolean(arr, off++, verEx);

        if (verEx) {
            GridCacheVersion drVer = ver.drVersion();

            assert drVer != null;

            UNSAFE.putInt(arr, off, drVer.topologyVersion());

            off += 4;

            UNSAFE.putInt(arr, off, drVer.nodeOrderAndDrIdRaw());

            off += 4;

            UNSAFE.putLong(arr, off, drVer.globalTime());

            off += 8;

            UNSAFE.putLong(arr, off, drVer.order());

            off += 8;
        }

        UNSAFE.putInt(arr, off, ver.topologyVersion());

        off += 4;

        UNSAFE.putInt(arr, off, ver.nodeOrderAndDrIdRaw());

        off += 4;

        UNSAFE.putLong(arr, off, ver.globalTime());

        off += 8;

        UNSAFE.putLong(arr, off, ver.order());

        off += 8;

        return off;
    }

    /**
     * @param ptr Offheap address.
     * @param verEx If {@code true} reads {@link GridCacheVersionEx} instance.
     * @return Version.
     */
    public static GridCacheVersion readVersion(long ptr, boolean verEx) {
        GridCacheVersion ver = new GridCacheVersion(UNSAFE.getInt(ptr),
            UNSAFE.getInt(ptr + 4),
            UNSAFE.getLong(ptr + 8),
            UNSAFE.getLong(ptr + 16));

        if (verEx) {
            ptr += 24;

            ver = new GridCacheVersionEx(UNSAFE.getInt(ptr),
                UNSAFE.getInt(ptr + 4),
                UNSAFE.getLong(ptr + 8),
                UNSAFE.getLong(ptr + 16),
                ver);
        }

        return ver;
    }

    /**
     * @param arr Array.
     * @param off Offset.
     * @param verEx If {@code true} reads {@link GridCacheVersionEx} instance.
     * @return Version.
     */
    public static GridCacheVersion readVersion(byte[] arr, long off, boolean verEx) {
        int topVer = UNSAFE.getInt(arr, off);

        off += 4;

        int nodeOrderDrId = UNSAFE.getInt(arr, off);

        off += 4;

        long globalTime = UNSAFE.getLong(arr, off);

        off += 8;

        long order = UNSAFE.getLong(arr, off);

        off += 8;

        GridCacheVersion ver = new GridCacheVersion(topVer, nodeOrderDrId, globalTime, order);

        if (verEx) {
            topVer = UNSAFE.getInt(arr, off);

            off += 4;

            nodeOrderDrId = UNSAFE.getInt(arr, off);

            off += 4;

            globalTime = UNSAFE.getLong(arr, off);

            off += 8;

            order = UNSAFE.getLong(arr, off);

            ver = new GridCacheVersionEx(topVer, nodeOrderDrId, globalTime, order, ver);
        }

        return ver;
    }

    /**
     * @param ptr Address.
     * @param size Size.
     * @return Bytes.
     */
    public static byte[] copyMemory(long ptr, int size) {
        byte[] res = new byte[size];

        UNSAFE.copyMemory(null, ptr, res, BYTE_ARRAY_DATA_OFFSET, size);

        return res;
    }

    /**
     * Returns a capacity that is sufficient to keep the map from being resized as
     * long as it grows no larger than expSize and the load factor is >= its
     * default (0.75).
     *
     * Copy pasted from guava. See com.google.common.collect.Maps#capacity(int)
     *
     * @param expSize Expected size of created map.
     * @return Capacity.
     */
    public static int capacity(int expSize) {
        if (expSize < 3)
            return expSize + 1;

        if (expSize < (1 << 30))
            return expSize + expSize / 3;

        return Integer.MAX_VALUE; // any large value
    }

    /**
     * Creates new {@link HashMap} with expected size.
     *
     * @param expSize Expected size of created map.
     * @param <K> Type of map keys.
     * @param <V> Type of map values.
     * @return New map.
     */
    public static <K, V> HashMap<K, V> newHashMap(int expSize) {
        return new HashMap<>(capacity(expSize));
    }

    /**
     * Creates new {@link LinkedHashMap} with expected size.
     *
     * @param expSize Expected size of created map.
     * @param <K> Type of map keys.
     * @param <V> Type of map values.
     * @return New map.
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int expSize) {
        return new LinkedHashMap<>(capacity(expSize));
    }

    /**
     * Creates new {@link HashSet} with expected size.
     *
     * @param expSize Expected size of created map.
     * @param <T> Type of elements.
     * @return New set.
     */
    public static <T> HashSet<T> newHashSet(int expSize) {
        return new HashSet<>(capacity(expSize));
    }

    /**
     * Creates new {@link LinkedHashSet} with expected size.
     *
     * @param expSize Expected size of created map.
     * @param <T> Type of elements.
     * @return New set.
     */
    public static <T> LinkedHashSet<T> newLinkedHashSet(int expSize) {
        return new LinkedHashSet<>(capacity(expSize));
    }
}
