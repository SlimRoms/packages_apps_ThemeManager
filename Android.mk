# Copyright (C) 2017 SlimRoms Project
# Copyright (C) 2017 Victor Lapin
# Copyright (C) 2017 Griffin Millender
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

LOCAL_PATH := $(call my-dir)
LOCAL_ASSETS_TEMP_PATH := $(call intermediates-dir-for,APPS,ThemeManager,,COMMON)/assets

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v7-appcompat \
    android-support-v4 \
    android-support-design \
    android-support-v7-recyclerview \
    glide \
    theme-core \
    lottie

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_RESOURCE_DIR := \
    $(LOCAL_PATH)/res \
    frameworks/support/v7/appcompat/res \
    frameworks/support/v7/recyclerview/res \
    frameworks/support/design/res \
    frameworks/theme-core/res

LOCAL_ASSET_DIR := $(LOCAL_ASSETS_TEMP_PATH)
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_CERTIFICATE := platform
LOCAL_PACKAGE_NAME := ThemeManager
LOCAL_AAPT_FLAGS := --auto-add-overlay \
    --extra-packages android.support.v7.appcompat:android.support.v7.recyclerview:android.support.design:com.slimroms.themecore

include $(BUILD_PACKAGE)
