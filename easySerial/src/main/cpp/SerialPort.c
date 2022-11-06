/*
 * Copyright 2009-2011 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * BASS Modified this file.
 */

#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include <strings.h>

#include "SerialPort.h"

#include "android/log.h"

static const char *TAG = "EasySerial";
#define LogD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LogE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

static speed_t getBaudRate(jint baudRate) {
    switch (baudRate) {
        case 0:
            return B0;
        case 50:
            return B50;
        case 75:
            return B75;
        case 110:
            return B110;
        case 134:
            return B134;
        case 150:
            return B150;
        case 200:
            return B200;
        case 300:
            return B300;
        case 600:
            return B600;
        case 1200:
            return B1200;
        case 1800:
            return B1800;
        case 2400:
            return B2400;
        case 4800:
            return B4800;
        case 9600:
            return B9600;
        case 19200:
            return B19200;
        case 38400:
            return B38400;
        case 57600:
            return B57600;
        case 115200:
            return B115200;
        case 230400:
            return B230400;
        case 460800:
            return B460800;
        case 500000:
            return B500000;
        case 576000:
            return B576000;
        case 921600:
            return B921600;
        case 1000000:
            return B1000000;
        case 1152000:
            return B1152000;
        case 1500000:
            return B1500000;
        case 2000000:
            return B2000000;
        case 2500000:
            return B2500000;
        case 3000000:
            return B3000000;
        case 3500000:
            return B3500000;
        case 4000000:
            return B4000000;
        default:
            return -1;
    }
}

/*
 * Class:     cedric_serial_SerialPort
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_com_bass_easySerial_SerialPort_close(JNIEnv *env, jobject thiz) {
    jclass SerialPortClass = (*env)->GetObjectClass(env, thiz);
    jclass FileDescriptorClass = (*env)->FindClass(env, "java/io/FileDescriptor");

    jfieldID mFdID = (*env)->GetFieldID(env, SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
    jfieldID descriptorID = (*env)->GetFieldID(env, FileDescriptorClass, "descriptor", "I");

    jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);
    jint descriptor = (*env)->GetIntField(env, mFd, descriptorID);

    LogD("close(fd = %d)", descriptor);
    close(descriptor);
}


JNIEXPORT jobject JNICALL
Java_com_bass_easySerial_SerialPort_open(JNIEnv *env, jobject thiz, jstring path, jint baudRate,
                                         jint flags) {
    int fd;// Linux串口文件句柄（本次整个函数最终的关键成果）
    speed_t speed;// 波特率类型的值
    jobject mFileDescriptor; // 文件句柄(最终返回的成果)

    //检查参数，获取波特率参数信息 [先确定好波特率]
    {
        speed = getBaudRate(baudRate);
        if (speed == -1) {
            LogE("无效的波特率");
            return NULL;
        }
    }

    //第一步：打开串口
    {
        jboolean isCopy;
        const char *path_utf = (*env)->GetStringUTFChars(env, path, &isCopy);
        LogD("打开串口 路径是:%s flags:0x%x", path_utf, O_RDWR | flags);
        fd = open(path_utf, O_RDWR | flags);// 打开串口的函数，O_RDWR(读 和 写)
        LogD("打开串口 open() fd = %d", fd);
        (*env)->ReleaseStringUTFChars(env, path, path_utf);// 释放操作
        if (fd == -1) {
            LogE("无法打开端口");
            return NULL;
        }
    }

    //第二步：获取和设置终端属性-配置串口设备
    /* TCSANOW：不等数据传输完毕就立即改变属性。
       TCSADRAIN：等待所有数据传输结束才改变属性。
       TCSAFLUSH：清空输入输出缓冲区才改变属性。
       注意：当进行多重修改时，应当在这个函数之后再次调用 tcgetattr() 来检测是否所有修改都成功实现。
     */
    {
        struct termios cfg;
        LogD("执行配置串口中...");
        if (tcgetattr(fd, &cfg)) {
            LogE("配置串口tcgetattr() 失败");
            close(fd);
            return NULL;
        }

        cfmakeraw(&cfg);// 将串口设置成原始模式，并且让fd(文件句柄 对串口可读可写)
        cfsetispeed(&cfg, speed);// 设置串口读取波特率
        cfsetospeed(&cfg, speed);// 设置串口写入波特率

        if (tcsetattr(fd, TCSANOW, &cfg)) { // 根据上面的配置，再次获取串口属性
            LogE("再配置串口tcgetattr() 失败");
            close(fd);
            return NULL;
        }
    }

    //第三步：构建FileDescriptor.java对象，并赋予丰富串口相关的值
    {
        jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
        jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
        jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
        //反射生成FileDescriptor对象，并赋值 (fd==Linux串口文件句柄) FileDescriptor的构造函数实例化
        mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
        (*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint) fd);// 这里的fd，就是打开串口的关键成果
    }
    return mFileDescriptor;// 把最终的成果，返回会Java层
}

JNIEXPORT jobject JNICALL
Java_com_bass_easySerial_SerialPort_open2(JNIEnv *env, jobject thiz, jstring path, jint baudRate,
                                          jint stopBits, jint dataBits,
                                          jint parity, jint flowCon, jint flags) {
    int fd;
    speed_t speed;
    jobject mFileDescriptor;

    /* Check arguments */
    {
        speed = getBaudRate(baudRate);
        if (speed == -1) {
            LogE("无效的波特率");
            return NULL;
        }
    }

    /* Opening device */
    {
        jboolean isCopy;
        const char *path_utf = (*env)->GetStringUTFChars(env, path, &isCopy);
        LogD("Opening serial port %s with flags 0x%x", path_utf, O_RDWR | flags);
        fd = open(path_utf, O_RDWR | flags);
        LogD("open() fd = %d", fd);
        (*env)->ReleaseStringUTFChars(env, path, path_utf);
        if (fd == -1) {
            LogE("无法打开端口");
            return NULL;
        }
    }

    /* Configure device */
    {
        struct termios cfg;
        LogD("执行配置串口中...");
        if (tcgetattr(fd, &cfg)) {
            LogE("配置串口tcgetattr() 失败");
            close(fd);
            return NULL;
        }

        cfmakeraw(&cfg);
        cfsetispeed(&cfg, speed);
        cfsetospeed(&cfg, speed);

        cfg.c_cflag &= ~CSIZE;
        switch (dataBits) {
            case 5:
                cfg.c_cflag |= CS5;    //Use 5-bit data bits
                break;
            case 6:
                cfg.c_cflag |= CS6;    //Use 6-bit data bits
                break;
            case 7:
                cfg.c_cflag |= CS7;    //Use 7-bit data bits
                break;
            default:
                cfg.c_cflag |= CS8;   //Use 8-bit data bits
                break;
        }

        switch (parity) {
            case 0:
                cfg.c_cflag &= ~PARENB;    // None parity
                break;
            case 1:
                cfg.c_cflag |= (PARODD | PARENB);   // Odd parity
                break;
            case 2:
                cfg.c_iflag &= ~(IGNPAR | PARMRK); // Even parity
                cfg.c_iflag |= INPCK;
                cfg.c_cflag |= PARENB;
                cfg.c_cflag &= ~PARODD;
                break;
            default:
                cfg.c_cflag &= ~PARENB;
                break;
        }

        switch (stopBits) {
            case 1:
                cfg.c_cflag &= ~CSTOPB;    // 1 bit stop bit
                break;
            case 2:
                cfg.c_cflag |= CSTOPB;    // 2 bit stop bit
                break;
            default:
                break;
        }

        // hardware flow control
        switch (flowCon) {
            case 0:
                cfg.c_cflag &= ~CRTSCTS;    // None flow control
                break;
            case 1:
                cfg.c_cflag |= CRTSCTS;    // Hardware flow control
                break;
            case 2:
                cfg.c_cflag |= IXON | IXOFF | IXANY;    // Software flow control
                break;
            default:
                cfg.c_cflag &= ~CRTSCTS;
                break;
        }


        if (tcsetattr(fd, TCSANOW, &cfg)) {
            LogE("再配置串口tcgetattr() 失败");
            close(fd);
            return NULL;
        }
    }

    /* Create a corresponding file descriptor */
    {
        jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
        jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
        jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
        mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
        (*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint) fd);
    }

    return mFileDescriptor;
}