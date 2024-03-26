/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.media3.muxer;

import androidx.media3.common.Metadata;
import androidx.media3.container.MdtaMetadataEntry;
import androidx.media3.container.Mp4LocationData;
import androidx.media3.container.Mp4OrientationData;
import androidx.media3.container.Mp4TimestampData;
import androidx.media3.container.XmpData;

/** Utilities for MP4 files. */
/* package */ final class Mp4Utils {
  /* Total number of bytes in an integer. */
  public static final int BYTES_PER_INTEGER = 4;

  /**
   * The maximum length of boxes which have fixed sizes.
   *
   * <p>Technically, we'd know how long they actually are; this upper bound is much simpler to
   * produce though and we'll throw if we overflow anyway.
   */
  public static final int MAX_FIXED_LEAF_BOX_SIZE = 200;

  /** The maximum value of a 32-bit unsigned int. */
  public static final long UNSIGNED_INT_MAX_VALUE = 4_294_967_295L;

  /**
   * The per-video timebase, used for durations in MVHD and TKHD even if the per-track timebase is
   * different (e.g. typically the sample rate for audio).
   */
  public static final long MVHD_TIMEBASE = 10_000L;

  private Mp4Utils() {}

  /** Converts microseconds to video units, using the provided timebase. */
  public static long vuFromUs(long timestampUs, long videoUnitTimebase) {
    return timestampUs * videoUnitTimebase / 1_000_000L; // (division for us to s conversion)
  }

  /** Converts video units to microseconds, using the provided timebase. */
  public static long usFromVu(long timestampVu, long videoUnitTimebase) {
    return timestampVu * 1_000_000L / videoUnitTimebase;
  }

  /** Returns whether a given {@link Metadata.Entry metadata} is supported. */
  public static boolean isMetadataSupported(Metadata.Entry metadata) {
    return metadata instanceof Mp4OrientationData
        || metadata instanceof Mp4LocationData
        || (metadata instanceof Mp4TimestampData
            && isMp4TimestampDataSupported((Mp4TimestampData) metadata))
        || (metadata instanceof MdtaMetadataEntry
            && isMdtaMetadataEntrySupported((MdtaMetadataEntry) metadata))
        || metadata instanceof XmpData;
  }

  private static boolean isMdtaMetadataEntrySupported(MdtaMetadataEntry mdtaMetadataEntry) {
    return mdtaMetadataEntry.typeIndicator == MdtaMetadataEntry.TYPE_INDICATOR_STRING
        || mdtaMetadataEntry.typeIndicator == MdtaMetadataEntry.TYPE_INDICATOR_FLOAT32;
  }

  private static boolean isMp4TimestampDataSupported(Mp4TimestampData timestampData) {
    return timestampData.creationTimestampSeconds <= UNSIGNED_INT_MAX_VALUE
        && timestampData.modificationTimestampSeconds <= UNSIGNED_INT_MAX_VALUE;
  }
}
