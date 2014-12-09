/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
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
 * #L%
 */
package org.ow2.chameleon.fuchsia.tools.shell.util;

import org.ow2.chameleon.fuchsia.tools.shell.util.exception.MandatoryArgumentException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FuchsiaGogoUtil {

    private static String reproduceChar(String ch, Integer amount){

        StringBuffer sb=new StringBuffer();

        for(int x=0;x<amount;x++){
            sb.append(ch);
        }

        return sb.toString();
    }

    public static StringBuilder createASCIIBox(String prolog, StringBuilder sb){
        StringBuilder result=new StringBuilder();

        StringReader sr=new StringReader(sb.toString());

        List<Integer> sizeColums=new ArrayList<Integer>();

        String line;
        try {

            BufferedReader br=new BufferedReader(sr);
            while((line=br.readLine())!=null){
                sizeColums.add(Integer.valueOf(line.length()));
            }

            Collections.sort(sizeColums);
            Collections.reverse(sizeColums);

            Integer maxColumn=sizeColums.isEmpty()?0:sizeColums.get(0);
            if(maxColumn>45) maxColumn=45;
            Integer prologSize=prolog.length();

            result.append(reproduceChar(" ",prologSize)+"."+reproduceChar("_",maxColumn)+"\n");

            sr=new StringReader(sb.toString());
            br=new BufferedReader(sr);
            int lineIndex=0;
            while((line=br.readLine())!=null){

                if(lineIndex==((Integer)(sizeColums.size()/2))){
                    result.append(prolog);
                }else {
                    result.append(reproduceChar(" ",prologSize));
                }

                result.append("|" + line + "\n");
                lineIndex++;
            }

            result.append(reproduceChar(" ",prologSize)+"|"+reproduceChar("_",maxColumn)+"\n");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }

    public static String getArgumentValue(String option, String... params) {
        boolean found = false;
        String value = null;

        for (int i = 0; i < params.length; i++) {

            /**
             * In case of a Null option, returns the last parameter.
             */
            if (option == null) {
                return params[params.length - 1];
            }

            if (i <= (params.length - 1) && params[i].equals(option)) {
                found = true;
                try {
                    value = params[i + 1];
                }catch (ArrayIndexOutOfBoundsException e){
                    value = "";
                }
                break;
            }
        }

        if (found) {
            return value;
        }
        return null;
    }

    public static String getArgumentValue(String option, Boolean mandatory,String... params) throws MandatoryArgumentException{

        String result=getArgumentValue(option,params);

        if(mandatory && result==null){
            throw new MandatoryArgumentException(String.format("The argument %s is mandatory",option));
        }

        return result;
    }
}
