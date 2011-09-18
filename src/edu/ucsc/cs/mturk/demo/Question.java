package edu.ucsc.cs.mturk.demo;

import java.util.ArrayList;

/*
 *  This class is for generating the string of a question, 
 *  according to the required format by Amazon Mechanical 
 *  Turk.
 */
public class Question {
    private String question;
    
    Question(ArrayList<Object> descriptions, int outputNum){
        question = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        question += "<QuestionForm xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd\">\n";
        question += "    <Question>\n";
        question += "        <QuestionIdentifier>Best_Description</QuestionIdentifier>\n";
        question += "        <DisplayName>Best Description</DisplayName>\n";
        question += "        <IsRequired>true</IsRequired>\n";
        question += "        <QuestionContent>\n";
        question += "            <FormattedContent><![CDATA[\n";
        question += "                <h1>Please choose " + outputNum + " best descriptions.</h1>\n";
        question += "            ]]></FormattedContent>\n";
        question += "        </QuestionContent>\n";
        question += "        <AnswerSpecification>\n";
        question += "        <SelectionAnswer>\n";
        question += "            <MinSelectionCount>" + outputNum + "</MinSelectionCount>\n";
        question += "            <MaxSelectionCount>" + outputNum + "</MaxSelectionCount>\n";
        question += "            <StyleSuggestion>checkbox</StyleSuggestion>\n";
        question += "            <Selections>\n";
        for(int j = 0; j < descriptions.size(); j++){
            question += "                <Selection>\n";
            question += "                    <SelectionIdentifier>" + "desc_identifier:" + descriptions.get(j).toString() + "</SelectionIdentifier>\n";
            question += "                    <Text>" + descriptions.get(j).toString() + "</Text>\n";
            question += "                </Selection>\n";
        }
        question += "            </Selections>\n";
        question += "        </SelectionAnswer>\n";
        question += "        </AnswerSpecification>\n";
        question += "    </Question>\n";
        question += "</QuestionForm>";
        }
    
    public String getQuestion(){
        return this.question;
    }
    
}
