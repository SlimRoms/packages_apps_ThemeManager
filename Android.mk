LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v7-appcompat \
    android-support-v4 \
    android-support-design \
    android-support-v7-recyclerview \
    glide \
    theme-core

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_RESOURCE_DIR := \
    $(LOCAL_PATH)/res \
    frameworks/support/v7/appcompat/res \
    frameworks/support/v7/recyclerview/res \
    frameworks/support/design/res \
    frameworks/opt/theme-core/res

LOCAL_PROGUARD_ENABLED := disabled
LOCAL_CERTIFICATE := platform
LOCAL_PACKAGE_NAME := ThemeManager
LOCAL_AAPT_FLAGS := --auto-add-overlay \
    --extra-packages android.support.v7.appcompat:android.support.v7.recyclerview:android.support.design:com.slimroms.themecore

include $(BUILD_PACKAGE)
