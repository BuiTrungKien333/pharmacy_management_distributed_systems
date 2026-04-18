package com.pharmacy.shared.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Object result; // Returned data if successful
    private Exception exception; // Error if any (e.g., Server crashes executing method)
    private int status;

    public boolean isSuccess() { return exception == null; }
}