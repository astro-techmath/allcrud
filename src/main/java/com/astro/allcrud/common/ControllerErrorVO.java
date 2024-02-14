package com.astro.allcrud.common;

import java.io.Serializable;

public record ControllerErrorVO(String error, String description) implements Serializable {
}
