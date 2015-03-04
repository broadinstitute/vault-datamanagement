package org.broadinstitute.dsde.vault.datamanagement.domain

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

@ApiModel(value = "An unmapped BAM")
case class UnmappedBAM
(
  @(ApiModelProperty@field)(value = "The files associated with this unmapped BAM, each with a unique user-supplied string key.", required = true)
  files: Map[String, String],

  @(ApiModelProperty@field)(value = "The metadata key-value pairs associated with this unmapped BAM.", required = true)
  metadata: Metadata,

  @(ApiModelProperty@field)(value = "The Vault ID of this unmapped BAM", required = false)
  id: String = null
  )

@ApiModel(value = "Metadata for a BAM (mapped or aggregated)")
case class Metadata
(
  @(ApiModelProperty@field)(required = true)
  ownerId: String,

  md5: String = null,

  project: String = null,

  individualAlias: String = null,

  sampleAlias: String = null,

  readGroupAlias: String = null,

  libraryName: String = null,

  sequencingCenter: String = null,

  platform: String = null,

  platformUnit: String = null,

  runDate: String = null,

  @(ApiModelProperty@field)(value = "indicates that this object supports arbitrary key-value pairs beyond the keys listed here. This is a hack right now" +
    " because I don't know the best way to represent varargs in Swagger.")
  additionalMetadata: String = null
  )
