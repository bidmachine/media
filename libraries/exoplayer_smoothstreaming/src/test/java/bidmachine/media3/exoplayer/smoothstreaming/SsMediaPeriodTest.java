/*
 * Copyright (C) 2018 The Android Open Source Project
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
package bidmachine.media3.exoplayer.smoothstreaming;

import static org.mockito.Mockito.mock;

import bidmachine.media3.common.C;
import bidmachine.media3.common.Format;
import bidmachine.media3.common.MimeTypes;
import bidmachine.media3.datasource.TransferListener;
import bidmachine.media3.exoplayer.drm.DrmSessionEventListener;
import bidmachine.media3.exoplayer.drm.DrmSessionManager;
import bidmachine.media3.exoplayer.smoothstreaming.manifest.SsManifest;
import bidmachine.media3.exoplayer.source.CompositeSequenceableLoaderFactory;
import bidmachine.media3.exoplayer.source.MediaSource.MediaPeriodId;
import bidmachine.media3.exoplayer.source.MediaSourceEventListener;
import bidmachine.media3.exoplayer.upstream.Allocator;
import bidmachine.media3.exoplayer.upstream.LoadErrorHandlingPolicy;
import bidmachine.media3.exoplayer.upstream.LoaderErrorThrower;
import bidmachine.media3.test.utils.MediaPeriodAsserts;
import bidmachine.media3.test.utils.MediaPeriodAsserts.FilterableManifestMediaPeriodFactory;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link SsMediaPeriod}. */
@RunWith(AndroidJUnit4.class)
public class SsMediaPeriodTest {

  @Test
  public void getSteamKeys_isCompatibleWithSsManifestFilter() {
    SsManifest testManifest =
        SsTestUtils.createSsManifest(
            SsTestUtils.createStreamElement(
                /* name= */ "video",
                C.TRACK_TYPE_VIDEO,
                createVideoFormat(/* bitrate= */ 200000),
                createVideoFormat(/* bitrate= */ 400000),
                createVideoFormat(/* bitrate= */ 800000)),
            SsTestUtils.createStreamElement(
                /* name= */ "audio",
                C.TRACK_TYPE_AUDIO,
                createAudioFormat(/* bitrate= */ 48000),
                createAudioFormat(/* bitrate= */ 96000)),
            SsTestUtils.createStreamElement(
                /* name= */ "text", C.TRACK_TYPE_TEXT, createTextFormat(/* language= */ "eng")));
    FilterableManifestMediaPeriodFactory<SsManifest> mediaPeriodFactory =
        (manifest, periodIndex) -> {
          MediaPeriodId mediaPeriodId = new MediaPeriodId(/* periodUid= */ new Object());
          return new SsMediaPeriod(
              manifest,
              mock(SsChunkSource.Factory.class),
              mock(TransferListener.class),
              mock(CompositeSequenceableLoaderFactory.class),
              /* cmcdConfiguration= */ null,
              mock(DrmSessionManager.class),
              new DrmSessionEventListener.EventDispatcher()
                  .withParameters(/* windowIndex= */ 0, mediaPeriodId),
              mock(LoadErrorHandlingPolicy.class),
              new MediaSourceEventListener.EventDispatcher()
                  .withParameters(/* windowIndex= */ 0, mediaPeriodId),
              mock(LoaderErrorThrower.class),
              mock(Allocator.class));
        };

    MediaPeriodAsserts.assertGetStreamKeysAndManifestFilterIntegration(
        mediaPeriodFactory, testManifest);
  }

  private static Format createVideoFormat(int bitrate) {
    return new Format.Builder()
        .setContainerMimeType(MimeTypes.VIDEO_MP4)
        .setSampleMimeType(MimeTypes.VIDEO_H264)
        .setAverageBitrate(bitrate)
        .build();
  }

  private static Format createAudioFormat(int bitrate) {
    return new Format.Builder()
        .setContainerMimeType(MimeTypes.AUDIO_MP4)
        .setSampleMimeType(MimeTypes.AUDIO_AAC)
        .setAverageBitrate(bitrate)
        .build();
  }

  private static Format createTextFormat(String language) {
    return new Format.Builder()
        .setContainerMimeType(MimeTypes.APPLICATION_MP4)
        .setSampleMimeType(MimeTypes.TEXT_VTT)
        .setLanguage(language)
        .build();
  }
}
