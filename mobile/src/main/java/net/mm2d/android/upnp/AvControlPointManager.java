/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp;

import android.os.Handler;
import android.os.Looper;

import net.mm2d.android.upnp.avt.MrControlPoint;
import net.mm2d.android.upnp.cds.MsControlPoint;
import net.mm2d.upnp.ControlPoint;
import net.mm2d.upnp.ControlPointFactory;
import net.mm2d.upnp.IconFilter;

import java.net.NetworkInterface;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * UPnP AVのControlPoint機能を管理する。
 *
 * <p>各DeviceTypeに特化した機能に対しControlPointWrapperに対し、
 * 一つのControlPointインスタンスで対応するため、
 * ControlPointのライフサイクルに関係する処理をこのクラスで管理する
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class AvControlPointManager {
    private static final IconFilter ICON_FILTER = list ->
            Collections.singletonList(Collections.max(list, DownloadIcon.COMPARATOR));

    @NonNull
    private final AtomicBoolean mInitialized = new AtomicBoolean();
    @Nullable
    private ControlPoint mControlPoint;
    @NonNull
    private final MsControlPoint mMsControlPoint = new MsControlPoint();
    @NonNull
    private final MrControlPoint mMrControlPoint = new MrControlPoint();

    public AvControlPointManager() {
    }

    /**
     * MsControlPointのインスタンスを返す。
     *
     * @return MsControlPoint
     */
    @NonNull
    public MsControlPoint getMsControlPoint() {
        return mMsControlPoint;
    }

    /**
     * MrControlPointのインスタンスを返す。
     *
     * @return MrControlPoint
     */
    @NonNull
    public MrControlPoint getMrControlPoint() {
        return mMrControlPoint;
    }

    /**
     * SSDP Searchを実行する。
     *
     * Searchパケットを一つ投げるのみであり、定期的に実行するにはアプリ側での実装が必要。
     */
    public void search() {
        if (!mInitialized.get()) {
            throw new IllegalStateException("ControlPoint is not initialized");
        }
        mControlPoint.search(null);
    }

    public void addPinnedDevice(@NonNull final String location) {
        if (!mInitialized.get()) {
            throw new IllegalStateException("ControlPoint is not initialized");
        }
        mControlPoint.tryAddPinnedDevice(location);
    }

    /**
     * 初期化が完了しているか。
     *
     * @return 初期化完了していればtrue
     */
    public boolean isInitialized() {
        return mInitialized.get();
    }

    /**
     * 初期化する。
     *
     * @param interfaces 使用するインターフェース
     */
    public void initialize(@Nullable final Collection<NetworkInterface> interfaces) {
        if (mInitialized.get()) {
            terminate();
        }
        mInitialized.set(true);
        final Handler handler = new Handler(Looper.getMainLooper());
        mControlPoint = ControlPointFactory.builder()
                .setInterfaces(interfaces)
                .setCallbackHandler(handler::post)
                .build();
        mControlPoint.setIconFilter(ICON_FILTER);

        mMsControlPoint.initialize(mControlPoint);
        mMrControlPoint.initialize(mControlPoint);

        mControlPoint.initialize();
    }

    /**
     * 処理を開始する。
     */
    public void start() {
        if (!mInitialized.get()) {
            throw new IllegalStateException("ControlPoint is not initialized");
        }
        mControlPoint.start();
    }

    /**
     * 処理を終了する。
     */
    public void stop() {
        if (!mInitialized.get()) {
            return;
        }
        mControlPoint.stop();
    }

    /**
     * 終了する。
     */
    public void terminate() {
        if (!mInitialized.getAndSet(false)) {
            return;
        }
        mMrControlPoint.terminate(mControlPoint);
        mMsControlPoint.terminate(mControlPoint);

        mControlPoint.terminate();
        mControlPoint = null;
    }
}
