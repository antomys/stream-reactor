/*
 * Copyright 2017-2025 Lenses.io Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.lenses.streamreactor.connect.cloud.common.formats.reader.converters

import io.lenses.streamreactor.connect.cloud.common.config.ReaderBuilderContext
import io.lenses.streamreactor.connect.cloud.common.formats.reader.Converter
import io.lenses.streamreactor.connect.cloud.common.model.Topic
import io.lenses.streamreactor.connect.cloud.common.model.location.CloudLocation
import io.lenses.streamreactor.connect.cloud.common.source.SourceWatermark
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.source.SourceRecord

import java.time.Instant

class TextConverter(
  writeWatermarkToHeaders: Boolean,
  watermarkPartition:      java.util.Map[String, String],
  topic:                   Topic,
  partition:               Integer,
  location:                CloudLocation,
  lastModified:            Instant,
) extends Converter[String] {
  override def convert(value: String, index: Long, lastLine: Boolean): SourceRecord = {
    val offset = SourceWatermark.offset(location, index, lastModified, lastLine)
    new SourceRecord(
      watermarkPartition,
      offset,
      topic.value,
      partition,
      null,
      null,
      Schema.STRING_SCHEMA,
      value,
      null,
      SourceWatermark.convertWatermarkToHeaders(writeWatermarkToHeaders, watermarkPartition, offset).orNull,
    )
  }
}

object TextConverter {

  def apply(
    input: ReaderBuilderContext,
  ): TextConverter =
    new TextConverter(
      input.writeWatermarkToHeaders,
      input.watermarkPartition,
      input.targetTopic,
      input.targetPartition,
      input.bucketAndPath,
      input.metadata.lastModified,
    )
}
