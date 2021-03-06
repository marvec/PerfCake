/*
 * Copyright 2010-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.perfcake.message;

import java.io.Serializable;

import org.perfcake.ObjectWithProperties;

/**
 * 
 * @author Lucie Fabriková <lucie.fabrikova@gmail.com>
 */
public class ReceivedMessage implements ObjectWithProperties, Serializable {

   private static final long serialVersionUID = 8426248937516343968L;

   // payload, id
   private Serializable payload;

   private String validatorId;

   public ReceivedMessage(Serializable payload, String validatorId) {
      this.payload = payload;
      this.validatorId = validatorId;
   }

   public Serializable getPayload() {
      return payload;
   }

   public void setPayload(Serializable payload) {
      this.payload = payload;
   }

   public String getValidatorId() {
      return validatorId;
   }

   public void setValidatorId(String validatorId) {
      this.validatorId = validatorId;
   }

   @Override
   public void setProperty(String property, String value) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

}
