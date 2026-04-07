variable "region" {
  description = "region"
  default     = "ap-northeast-2"
}

variable "prefix" {
  description = "Prefix for all resources"
  default     = "devcos-team03"
}
//도메인은 바꿔야함
variable "team03_domain" {
  description = "team03_domain"
  default     = "api.dabjeongneo.site"
}