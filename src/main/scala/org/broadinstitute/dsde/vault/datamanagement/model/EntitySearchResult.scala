package org.broadinstitute.dsde.vault.datamanagement.model

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

@ApiModel(value = "An entity search result")
case class EntitySearchResult
(
  @(ApiModelProperty@field)(value = "The unique id for this entity.", required = true)
  guid: String,

  @(ApiModelProperty@field)(value = "The type of entity.", required = true)
  `type`: String
  )
