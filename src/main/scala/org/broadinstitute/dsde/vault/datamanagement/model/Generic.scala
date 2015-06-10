package org.broadinstitute.dsde.vault.datamanagement.model

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

@ApiModel("system-generated entity attributes")
case class GenericSysAttrs(
  @(ApiModelProperty@field)("optional BOSS ID")
  bossID: Option[String],
  @(ApiModelProperty@field)("when the entity was created (mSecs since epoch)")
  createdDate: Long,
  @(ApiModelProperty@field)("openAM commonName of entity creator")
  createdBy: String,
  @(ApiModelProperty@field)(value="when the entity was most recently modified (mSecs since epoch)",dataType="Long")
  modifiedDate: Option[Long],
  @(ApiModelProperty@field)("openAM commonName of most recent modifier")
  modifiedBy: Option[String] )

@ApiModel("something that lives in the vault")
case class GenericEntity(
  @(ApiModelProperty@field)("the globally unique vault ID for the entity")
  guid: String,
  @(ApiModelProperty@field)("the entity type (open vocabulary - not enumerated)")
  entityType: String,
  @(ApiModelProperty@field)("some system-generated 'when's and 'who's")
  sysAttrs: GenericSysAttrs,
  @(ApiModelProperty@field)("an open set of metadata attributes of the entity")
  attrs: Option[Map[String,String]],
  @(ApiModelProperty@field)("an optional set of related entities")
  relEnts: Option[Seq[GenericRelEnt]])

@ApiModel("a directed relationship between two entities")
case class GenericRelationship(
  @(ApiModelProperty@field)("the type of relationship (open vocabulary - not enumerated)")
  relationType: String,
  @(ApiModelProperty@field)("an open set of metadata attributes of the relationship")
  attrs: Option[Map[String,String]] )

@ApiModel("a description of a relationship and of the entity targeted by the relationship")
case class GenericRelEnt(
  @(ApiModelProperty@field)("the relationship")
  relationship: GenericRelationship,
  @(ApiModelProperty@field)("the entity so-related")
  entity: GenericEntity )


@ApiModel("a new entity to create")
case class GenericEntityIngest(
  @(ApiModelProperty@field)("the entity type (open vocabulary - not enumerated)")
  entityType: String,
  @(ApiModelProperty@field)("optional BOSS ID")
  bossID: Option[String],
  @(ApiModelProperty@field)("an open set of metadata attributes of the entity")
  attrs: Map[String,String] )

@ApiModel("a new relationship to create")
case class GenericRelationshipIngest(
  @(ApiModelProperty@field)("the type of relationship (open vocabulary - not enumerated)")
  relationType: String,
  @(ApiModelProperty@field)("the vault ID of the upstream entity (or a reference of the form $0, $1, $2... to refer to an entity that will be created as a part of this ingest)")
  ent1: String,
  @(ApiModelProperty@field)("the vault ID of the downstream entity (or a reference of the form $0, $1, $2... to refer to an entity that will be created as a part of this ingest)")
  ent2: String,
  @(ApiModelProperty@field)("an open set of metadata attributes of the relationship")
  attrs: Map[String,String] )

@ApiModel("a small chunk of a client's object model to be created all at once")
case class GenericIngest(
  @(ApiModelProperty@field)("some entities to create")
  entities: Option[List[GenericEntityIngest]],
  @(ApiModelProperty@field)("some relationships to create among the new (or previously existing) entities")
  relations: Option[List[GenericRelationshipIngest]] )

@ApiModel("a metadata attribute name and value")
case class GenericAttributeSpec(
  @(ApiModelProperty@field)("the name")
  name: String,
  @(ApiModelProperty@field)("the value")
  value: String )

@ApiModel("a query for a type of entity with optional metadata attribute value")
case class GenericEntityQuery(
  @(ApiModelProperty@field)("the entity type")
  entityType: String,
  @(ApiModelProperty@field)("optional metadata attribute spec")
  attrSpec: Seq[GenericAttributeSpec],
  @(ApiModelProperty@field)("return metadata attributes, or skip it")
  expandAttrs: Boolean,
  @(ApiModelProperty@field)("optional value to specify the depth of entity relationships to include")
  depth: Option[Int] )
