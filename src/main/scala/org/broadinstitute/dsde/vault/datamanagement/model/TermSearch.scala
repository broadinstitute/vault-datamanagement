package org.broadinstitute.dsde.vault.datamanagement.model

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

@ApiModel(value = "Term search")
case class TermSearch
(
  @(ApiModelProperty@field)(value = "The key.", required = true)
  key: String,

  @(ApiModelProperty@field)(value = "The value.", required = true)
  value: String
  )
