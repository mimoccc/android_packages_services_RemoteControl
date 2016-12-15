package com.android.tv.remotecontrol.service;
import android.util.MutableInt;
import android.hardware.input.InputManager.InputDeviceListener;
import android.hardware.input.InputManager;
import android.view.InputDevice;
import android.app.Service;
import android.util.Log;
import android.content.Context;
import android.os.IBinder;
import android.content.Intent;
import android.system.Os;
import android.system.OsConstants;
import java.io.FileDescriptor;
import android.system.ErrnoException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * A local service to listen for RemoteControlInputs.
 */
public class RemoteControlInputService extends Service implements InputDeviceListener
{
    private static final String TAG = "RemoteControlInputService";
    private static final boolean DEBUG = true;
    private static final String KEYMAP_FILE = "/system/etc/rc_keymaps/sky_plus_rev8";


    private FileDescriptor getEvDevFd(InputDevice inputDevice)
    {

        File inputDir = new File("/dev/input");
        File[] files = inputDir.listFiles();
        Log.d(TAG, "inputDir Length=" + files.length);
        FileDescriptor retFd = null;
        FileReader fr = null;
        BufferedReader br = null;
        for (File file : files) {
            try {
                fr = new FileReader("/sys/class/input/" + file.getName() + "/device/name");
                br = new BufferedReader(fr);
                if (inputDevice.getName().equals(br.readLine())) {
                    Log.d(TAG, "Found evDev:"+file.getAbsolutePath());
                    fr.close();
                    br.close();
                    return Os.open(file.getAbsolutePath(),OsConstants.O_RDWR,OsConstants.S_IRGRP);

                }
            } catch ( Exception e ) {
                e.printStackTrace();
            //} catch ( ErrnoException e) {
            }

            try {
                fr.close();
                br.close();
            } catch ( IOException e ) {
            }

        }
        Log.d(TAG, "retFdh=" + retFd);
        return retFd;


    }
    private void processInputDevices (InputManager inputManager, int[] deviceIds)
    {

        if (DEBUG) Log.d(TAG, "processInputDevices");
        InputDevice inputDevice = null;
        if (inputManager != null) {
            for (int i = 0; i < deviceIds.length; ++i) {
                inputDevice = inputManager.getInputDevice(deviceIds[i]);
                if (inputDevice != null && inputDevice.getProductId() == 0x0001
                    && inputDevice.getVendorId() == 0x1784) {
                        break;
                }
            }
        }
        Log.d(TAG,  inputDevice.toString());
        writeKeymap(inputDevice);


    }

    private void writeKeymap(InputDevice inputDevice)
    {

        try {
            FileDescriptor evFd = getEvDevFd(inputDevice);
            FileDescriptor fd = Os.open(KEYMAP_FILE,OsConstants.O_RDONLY,OsConstants.S_IRUSR);
            if (evFd.valid()) {
                if (DEBUG) Log.d(TAG, "writeKeymap: fd=" + fd);
                int[] arr = new int[2];
                arr[0] = 0xC84;
                arr[1] = 0x179; // KEY_TV



                /*if (Os.ioctlIntArray(evFd,OsConstants.EVIOCSKEYCODE, arr) == -1) {
                    Log.d(TAG, "You Broke It");
                }*/
                Os.close(fd);
                Os.close(evFd);

            }
        } catch ( ErrnoException e) {
            e.printStackTrace();

        }

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (DEBUG) Log.d(TAG, "onStartCommand");
        InputManager inputManager = (InputManager)getSystemService(Context.INPUT_SERVICE);
        if (inputManager == null){
            Log.e(TAG, "Cannot Get InputManager Service");
        }

        inputManager.registerInputDeviceListener(this, null);
        processInputDevices(inputManager, inputManager.getInputDeviceIds());
        return START_STICKY;

    }

    @Override
    public void onDestroy()
    {
        if (DEBUG) Log.d(TAG, "onDestroy");
        InputManager inputManager = (InputManager)getSystemService(Context.INPUT_SERVICE);
        if (inputManager == null){
            Log.e(TAG, "Cannot Get InputManager Service");
        }
        inputManager.unregisterInputDeviceListener(this);
        super.onDestroy();
    }

    // Implementation of InputManager.InputDeviceListener.onInputDeviceAdded()
    @Override
    public void onInputDeviceAdded(int deviceId)
    {
         if (DEBUG) Log.d(TAG, "Device added: " + deviceId);
         InputManager inputManager = (InputManager)getSystemService(Context.INPUT_SERVICE);
         if (inputManager == null){
            Log.e(TAG, "Cannot Get InputManager Service");
         }
         int[] ids = { deviceId };
         processInputDevices(inputManager, ids);

    }

    // Implementation of InputManager.InputDeviceListener.onInputDeviceChanged()
    @Override
    public void onInputDeviceChanged(int deviceId) {
        if (DEBUG) Log.d(TAG, "Device changed: " + deviceId);
    }

    // Implementation of InputManager.InputDeviceListener.onInputDeviceRemoved()
    @Override
    public void onInputDeviceRemoved(int deviceId) {
        if (DEBUG) Log.d(TAG, "Device removed: " + deviceId);

    }

};