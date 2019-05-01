/*
 * Copyright 2018 lusidity inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.lusidity.services.common;

import com.lusidity.framework.text.StringX;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

public class Statements {

    private Statements() {
        super();
    }

    public static String toHtml(StatementTypes statementType)
            throws JSONException {
        StringBuilder results = new StringBuilder();
        JSONObject statements = Statements.getStatement(statementType);

        results.append("<table style=\"border-collapse: collapse; width: 98%;color: #333333;font-size: 12px; " +
                "font-family: Helvetica, Arial, sans-serif;\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">");
        results.append("<tr>");
        results.append("<td style=\"font-weight:bold;font-size: 16px;padding-bottom:5px;color:#333333;\">");

        if (statements.has("title") && !StringX.isBlank(statements.getString("title"))) {
            String title = statements.getString("title");
            results.append(title);

            results.append("</td></tr>");
        }

        if (statements.has("content") && (statements.getJSONArray("content").length() > 0)) {
            JSONArray content = statements.getJSONArray("content");

            int x = content.length();
            for (int i = 0; i < x; i++) {
                JSONObject statement = content.getJSONObject(i);
                Statements.createRow(statement, results);
            }
        }

        results.append("</table>");

        return results.toString();
    }

    private static void createRow(JSONObject statement, StringBuilder results) throws JSONException {

        if (statement.has("title") && !StringX.isBlank(statement.getString("title"))) {
            results.append("<tr>");
            results.append("<td style=\"font-weight:normal;font-size: 14px;color: #868686;padding-top: 5px;\">");

            String title = statement.getString("title");
            results.append(title);


            results.append("</td></tr>");
        }

        if (statement.has("statements") && (statement.getJSONArray("statements").length() > 0)) {
            JSONArray items = statement.getJSONArray("statements");
            int x = items.length();
            for (int i = 0; i < x; i++) {
                String item = items.getString(i);

                results.append("<tr>");
                results.append("<td style=\"font-weight:normal;font-size: 12px;color: #333333;padding: 0 5px 5px 5px;\">");

                results.append(item);

                results.append("</td></tr>");
            }
        }
    }

    public enum StatementTypes {
        termsofservice,
        privacypolicy,
        about
    }

    /**
     * Returns the requested statement.
     *
     * @param statementType The statement type.
     * @return The requested statement.
     * @throws JSONException
     */
    public static JSONObject getStatement(StatementTypes statementType)
            throws JSONException {
        JSONObject result = null;
        if (StatementTypes.privacypolicy == statementType) {
            result = new JSONObject();
            result.put("title", "Privacy Policy");
            result.put("content", Statements.getPrivacyPolicy());
        } else if (StatementTypes.termsofservice == statementType) {
            result = new JSONObject();
            result.put("title", "Terms of Service");
            result.put("content", Statements.getTermsOfService());
        } else if (StatementTypes.about == statementType) {
            result = new JSONObject();
            result.put("title", "About");
            result.put("content", Statements.getAbout());
        }

        return result;
    }

    /**
     * About
     *
     * @return About
     * @throws JSONException
     */
    @SuppressWarnings("OverlyLongMethod")
    private static JSONArray getAbout() throws JSONException {
        JSONArray results = new JSONArray();

        Collection<String> statement = new ArrayList<String>();
        statement.add("Welcome to lusidity! lusidity is a new kind of search engine that brings the Internet" +
                " technologies of tomorrow to you today.");
        results.put(Statements.createObject(null, statement));

        statement = new ArrayList<String>();
        statement.add("lusidity has several key features that make it different than other popular search engines:");
        results.put(Statements.createObject(null, statement));

        statement = new ArrayList<String>();
        statement.add("<ul>"
                + "<li>"
                + "lusidity is focused on <strong>knowledge</strong>, not content. Traditional search engines simply" +
                " find Web pages that may be relevant to your query. lusidity integrates knowledge from some of the" +
                " Web's best open data sources to respond to your queries with clear, concise, and relevant" +
                " summaries of information."
                + "</li>"
                + "<li>"
                + "lusidity connects to some of the Web's most popular and trusted services and retailers to allow" +
                " you to quickly take <strong>actions</strong>, like buying an item or watching a movie, from" +
                " your search results."
                + "</li>"
                + "<li>"
                + "lusidity was designed from the ground up to embrace and enrich today's <strong>mobile</strong>" +
                " computing paradigm. Whether you are using your smart-phone on a commuter train, relaxing at home" +
                " on the sofa with your tablet computer, or working at the office on a traditional computer," +
                " lusidity adapts to present information and options in a way that fits your device."
                + "</li>"
                + "<li>"
                + "There is <strong>no advertising</strong> in lusidity. If you search for a product or service," +
                " lusidity will help you find it, but we will not subject you to advertisements (especially the" +
                " poorly-targeted, borderline illiterate, crudely-animated, and often deceptive or misleading" +
                " advertisements that clutter so many Web pages today)."
                + "</li>"
                + "<li>"
                + "Most of the Web's most popular search engines, social networks, and other sites have an opaque" +
                " business model: they seem to be free, but they are really based on collecting as much" +
                " information as possible about you, then" +
                " using it to target you in marketing and advertising campaigns, directly or through" +
                " loosely-vetted partners. In contrast, lusidity has a simple business model: we earn a small" +
                " commission from our hand-selected partners (who we personally do business with on a regular" +
                " basis) when you take actions like buying a product or service through lusidity."
                + "</li>"
                + "<li>"
                + "lusidity protects your <strong>privacy</strong> and <strong>security</strong>. Your entire" +
                " session with lusidity is protected by the same technologies used in on-line banking and other" +
                " sensitive transactions. Our \"above-the-fold\" knowledge boxes are based on information from" +
                " the Web's most trusted and reputable sites and cannot be manipulated by advertisers, spammers," +
                " and scammers using so-called Search Engine Optimization (SEO) techniques. Finally, <strong>" +
                "we do not track</strong> your searches or other activities (even if you make a purchase or" +
                " use one of our other actions)."
                + "</li>"
                + "</ul>");
        results.put(Statements.createObject(null, statement));

        statement = new ArrayList<String>();
        statement.add("lusidity is a work-in-process. This release is intended to gather some feedback from real" +
                " end-users like yourself and test our technology and infrastructure, so you should be aware" +
                " of the following limitations:");
        results.put(Statements.createObject(null, statement));

        statement = new ArrayList<String>();
        statement.add("<ul>"
                + "<li>"
                + "lusidity currently only has knowledge of books, movies, and general products. Future releases will" +
                " include more knowledge from more data sources across more categories, including music, TV programs," +
                " people, and organizations (including businesses)."
                + "</li>"
                + "<li>"
                + "lusidity currently only shows purchase/rental options from Amazon.com. Future releases will" +
                " include connections to more of the most popular retailers on the Web."
                + "</li>"
                + "</ul>");
        results.put(Statements.createObject(null, statement));

        statement = new ArrayList<String>();
        statement.add("Please use the feedback button (the leftmost button in the lusidity toolbar at the top" +
                " of every lusidity page) to report any problems or offer suggestions that you might have." +
                "  Thank you for your interest in lusidity!");
        results.put(Statements.createObject(null, statement));

        return results;
    }

    /**
     * Terms of Service
     *
     * @return Terms of Service
     * @throws JSONException
     */
    @SuppressWarnings("OverlyLongMethod")
    private static JSONArray getTermsOfService() throws JSONException {
        JSONArray results = new JSONArray();

        Collection<String> statement = new ArrayList<String>();
        statement.add("Effective 1 August 2013");
        results.put(Statements.createObject(null, statement));

        statement = new ArrayList<String>();
        statement.add("PLEASE READ THE TERMS OF SERVICE BELOW. THESE TERMS OF SERVICE, INCLUDING ANY REVISED" +
                " AGREEMENTS THAT WE MAY POST FROM TIME TO TIME, STATE THE AGREEMENT (AGREEMENT) UNDER WHICH VENIO" +
                " INC. d/b/a EmailTemplate (lusidity) PROVIDES YOU WITH VARIOUS SERVICES, CURRENTLY LOCATED AT " +
                "LUSIDITY.COM, AND ANY OTHER RELATED OR SUCCESSOR SITES (SITE). ALL SERVICES PROVIDED BY LUSIDITY" +
                " ON THE SITE ARE COLLECTIVELY REFERRED TO AS SERVICES. BY ACCESSING, BROWSING AND/OR USING OUR SITE" +
                " AND/OR SERVICES, YOU ARE DEEMED TO ACCEPT THE TERMS OF SERVICE AND AGREE TO BE BOUND BY THIS" +
                " AGREEMENT WITH RESPECT TO THE USE OF THAT SITE. IF YOU DO NOT WISH TO BE BOUND BY THIS AGREEMENT," +
                " YOU MAY NOT ACCESS, BROWSE OR USE THE SITE OR ANY SERVICES WE PROVIDE.");
        statement.add("You may not use the Services and may not accept these terms if (a) you are not of legal age" +
                " in your jurisdiction to form a legally binding contract with lusidity, or (b) you are not allowed" +
                " to receive the Services under the laws of the United States, your country of residence, the" +
                " country in which you are located or are otherwise legally barred.");
        results.put(Statements.createObject("Venio Inc. - Terms of Service for Your Personal Use", statement));

        statement = new ArrayList<String>();
        statement.add("The contents of our Site (Content) are intended for the personal, noncommercial use of" +
                " our users. All right, title and interest to the Content displayed on our Site, including but" +
                " not limited to the Sites look and feel, data, information, text, graphics, images, sound or video" +
                " materials, photographs, designs, trademarks, service marks, trade names, URLs and content provided" +
                " by third parties, are the property of lusidity, or the respective third parties, and are protected" +
                " by copyright, trademark, patent or other proprietary rights and laws.");
        statement.add("Except as expressly authorized by lusidity, you agree not to copy, modify, rent, lease, loan," +
                " sell, assign, distribute, perform, display, license, reverse engineer or create derivative works" +
                " based on the Site or any content (including without limitation any software) available" +
                " through the Site.");
        results.put(Statements.createObject("Ownership and Protection of Intellectual Property Rights", statement));

        statement = new ArrayList<String>();
        statement.add("lusidity may make changes to this Agreement from time to time at its sole discretion. Each" +
                " time changes are made to this Agreement, a notice will be posted on the home page at" +
                " lusidity.com. Your continued use of our Site and/or the Services following the posting of" +
                " changes constitutes your acceptance of any such changes. You can review the most current version" +
                " of this Agreement here. Please check this page from time to time for current terms of use.");
        statement.add("");
        results.put(Statements.createObject("Changes to This Agreement", statement));

        statement = new ArrayList<String>();
        statement.add("The information that we obtain through your use of our Site, whether through the" +
                " registration process or otherwise, is subject to our Privacy Policy (Privacy Policy)." +
                " The lusidity Privacy Policy can be viewed on our Privacy Policy page. The Privacy Policy" +
                " contains terms and conditions that govern our collection and use of the information you provide" +
                " to us, including our respective rights relative to that information. Please review the" +
                " applicable Privacy Policy before you use our Site. If you are unwilling to accept the terms and" +
                " conditions of the Privacy Policy, please do not use our Site.");
        results.put(Statements.createObject("Our Privacy Policy", statement));


        statement = new ArrayList<String>();
        statement.add("Your use of our Site is also subject to the other policies, disclaimers and guidelines" +
                " we post on such Site from time to time.");
        results.put(Statements.createObject("Other Policies", statement));


        statement = new ArrayList<String>();
        statement.add("You are hereby granted a personal, nonexclusive, nontransferable, revocable, limited license" +
                " to view, reproduce, print, cache, store and distribute content retrieved from our Site via a" +
                " generally available consumer web browser, provided that you do not (and do not allow any third" +
                " party to) copy, modify, create a derivative work of, reverse engineer, reverse assemble or" +
                " otherwise attempt to discover any source code, sell, assign, sublicense, grant a security" +
                " interest in or otherwise transfer any right in the Services or remove or obscure the copyright" +
                " notice or other notices displayed on the content. You may not reproduce, print, cache, store" +
                " or distribute content retrieved from the Site in any way, for any commercial use without the prior" +
                " written permission of lusidity or the copyright holder identified in the relevant copyright notice.");
        statement.add("Except as expressly provided in this Agreement, nothing contained in this Agreement or on" +
                " the Site shall be construed as conferring any other license or right, expressly, by implication," +
                " by estoppel or otherwise, with respect to any of lusidity's content or under any third party's" +
                " content. Any rights not expressly granted herein are reserved.");
        results.put(Statements.createObject("License Grant to Access Content on Our Site", statement));

        statement = new ArrayList<String>();
        statement.add("Customer support is not offered for the Site or the Services.");
        results.put(Statements.createObject("Customer Service", statement));

        statement = new ArrayList<String>();
        statement.add("You are solely responsible for obtaining Internet access to the Site and the equipment" +
                " and software necessary to use and enjoy the Site and the Services, including payment of any" +
                " Internet service provider fees and telecommunication charges.");
        results.put(Statements.createObject("Access to the Site and Services", statement));

        statement = new ArrayList<String>();
        statement.add("lusidity does not have an established maximum number of transactions, transmissions, or" +
                " level of storage used in connection with the Services. lusidity reserves the right to set such" +
                " limits, in its sole discretion, at any time.");
        statement.add("You may not send automated queries of any sort to the Site without express advance written" +
                " permission from lusidity.environment. Note that sending automated queries includes," +
                " among other things:");
        statement.add("using any software which sends queries to our sites to determine how a website or webpage" +
                " ranks for various queries;");
        statement.add("meta-searching; and");
        statement.add("performing offline searches on the Site.");
        results.put(Statements.createObject("Transactions/Transmissions/Storage/No Automated Querying", statement));

        statement = new ArrayList<String>();
        statement.add("A central part of the Site and the Services includes links to other websites or resources" +
                " and information (including gadgets, financial information) provided by others. Because lusidity" +
                " has no control over such sites and resources, you acknowledge and agree that lusidity is not" +
                " responsible for the availability of such external sites or resources, and does not endorse and" +
                " is not responsible or liable for any content, advertising, products, or other materials on or" +
                " available from such sites or resources. You further acknowledge and agree that lusidity shall not" +
                " be responsible or liable, directly or indirectly, for any damage or loss caused by or in" +
                " connection with use of or reliance on any such content, goods or services available on or through" +
                " any such site or resource. Finally, you acknowledge that such external sites and third-party" +
                " tools usually have their own terms and conditions, including privacy policies, over which lusidity" +
                " has no control and which will govern your rights and obligations with respect to the use of such.");
        results.put(Statements.createObject("Links to, and Resources/Data from, Others", statement));

        statement = new ArrayList<String>();
        statement.add("Your dealings with advertisers and third-party vendors found on or through the Site and the" +
                " Services, including your participation in promotions, the purchase of goods, and any terms," +
                " conditions, warranties or representations associated with such activities, are solely between you" +
                " and the third party. lusidity does not make any representations or warranties with respect to any" +
                " goods or web sites that may be obtained from such third parties, and you agree that lusidity will" +
                " have no liability for any loss or damage of any kind incurred as a result of any activities you" +
                " undertake in connection with the use of or reliance on any content, goods, services, information" +
                " or other materials available, or through such third parties, on our Site. You acknowledge that such" +
                " external sites usually have their own terms and conditions, including privacy policies, over which" +
                " lusidity has no control and which will govern your rights and obligations with respect to the use" +
                " of those Web sites.<br/><br/>Any displayed product pricing and availability information is accurate at " +
                "the moment it is displayed, but is subject to change. Any price and availability information" +
                " displayed on partner e-commere sites (e.g. Amazon) at the time of purchase will apply to the" +
                " purchase of the product.");
        results.put(Statements.createObject("Your Contact with Advertisers or Third-Party Vendors", statement));

        statement = new ArrayList<String>();
        statement.add("You agree to comply with all applicable laws regarding your use of our Site and the Services" +
                " including, without limitation, all applicable laws (as well as associated licenses and approvals)" +
                " regarding the transmission of technical data exported from the United States or the country in" +
                " which you reside.");
        statement.add("You agree that you will not:");
        statement.add("(a) use the Site or the Services in any manner that harms us or the parties with which we contract;");
        statement.add("(b) modify or reroute the Site or the Services, or attempt to do so;");
        statement.add("(c) overburden, harm, limit, or disable the Site or the Services, including associated" +
                " networks, or otherwise impair anyone's use of the Site or the Services.  Passwords and Security");
        statement.add("You should not provide your password(s) to anyone else and are responsible for maintaining" +
                " the confidentiality of your password(s) used for the Services. Therefore, you agree that you are" +
                " solely responsible for all usage and activities under your account.");

        results.put(Statements.createObject("Compliance with Laws/Restrictions in Use", statement));

        statement = new ArrayList<String>();
        statement.add("lusidity takes intellectual property issues seriously and has a policy to respond to notices" +
                " of alleged infringement that comply with applicable international intellectual property law" +
                " (including the U.S. Digital Millennium Copyright Act) and to terminating the accounts of repeat" +
                " infringers. Our policy is located here. Please note: all notices and transmissions under such policy" +
                " that are not relevant will not receive any response.");
        results.put(Statements.createObject("Intellectual Property Violations", statement));

        statement = new ArrayList<String>();
        statement.add("You may find some content, on the Site and viewed through the Services to be offensive or" +
                " objectionable, and you use the Site and the Services at your sole risk with respect to such.");
        results.put(Statements.createObject("Review of Content", statement));

        statement = new ArrayList<String>();
        statement.add("lusidity reserves the right at any time and from time to time to modify, suspend, discontinue" +
                " or terminate the Services (or any part thereof) with or without notice. You agree that lusidity" +
                " will not be liable to you or to any third party for any modification, suspension, discontinuation" +
                " or termination of the Services.");
        results.put(Statements.createObject("Modifications to the Services", statement));

        statement = new ArrayList<String>();
        statement.add("You agree that lusidity, in its sole discretion, may terminate your access to any of the" +
                " Services for any reason, including, without limitation, for lack of use or if lusidity believes" +
                " that you have violated or acted inconsistently with the letter or spirit of this Agreement." +
                " You agree that any termination of your access to the Services may be effected without prior" +
                " notice, and acknowledge and agree that lusidity may bar any further access to such files or the" +
                " Services. If you use the Site in violation of this Agreement, lusidity may, in its sole" +
                " discretion, retain all data collected from your use of the Site. Further, you agree that lusidity" +
                " shall not be liable to you or to any third party for the discontinuation or termination of your" +
                " access to the Services, or collection of information notwithstanding in the case of your violation" +
                " of this Agreement, even if advised of a claim for damages.");
        statement.add("You may cease using the Services at any time." +
                "  You do not need to inform lusidity if you cease using the Services.");
        results.put(Statements.createObject("Termination of Your Access to the Services", statement));

        statement = new ArrayList<String>();
        statement.add("BY USING THE SITE AND THE SERVICES YOU UNDERSTAND AND AGREE THAT:");
        statement.add("THE SERVICES ARE PROVIDED FOR INFORMATIONAL PURPOSES ONLY. NO CONTENT ON OUR SITE IS INTENDED" +
                " TO CONSTITUTE PROFESSIONAL ADVICE, WHETHER MEDICAL, FINANCIAL, LEGAL OR OTHERWISE. LUSIDITY AND THOSE" +
                " POSTING OR OTHERWISE PROVIDING INFORMATION, SERVICES OR MATERIAL ON OUR SITE ARE NOT RESPONSIBLE OR" +
                " LIABLE FOR ANY CONSEQUENCES RELATING DIRECTLY OR INDIRECTLY TO ANY ACTION OR INACTION YOU TAKE BASED" +
                " ON THE INFORMATION, SERVICES OR OTHER MATERIAL ON OUR SITE.");
        statement.add("YOUR USE OF THE SERVICES IS AT YOUR SOLE RISK. THE SERVICES ARE PROVIDED ON AN AS IS AND AS AVAILABLE BASIS. LUSIDITY EXPRESSLY DISCLAIMS ALL WARRANTIES OF ANY KIND, WHETHER EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF TITLE, MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON- INFRINGEMENT.");
        statement.add("LUSIDITY MAKES NO REPRESENTATION OR WARRANTY THAT THE SERVICES WILL MEET YOUR REQUIREMENTS, THAT THE SERVICES WILL BE UNINTERRUPTED, SECURE, CURRENT OR ERROR-FREE, THAT THE RESULTS THAT MAY BE OBTAINED FROM THE USE OF THE SERVICES WILL BE ACCURATE, TIMELY, USEFUL OR RELIABLE, OR THAT THE QUALITY OF ANY POSTINGS, PRODUCTS, SERVICES, INFORMATION OR OTHER MATERIAL OBTAINED BY YOU THROUGH THE SERVICES WILL MEET YOUR NEEDS.");
        statement.add("ANY MATERIAL OBTAINED THROUGH THE USE OF THE SERVICES IS DONE AT YOUR OWN DISCRETION AND RISK, AND YOU WILL BE SOLELY RESPONSIBLE FOR ANY DAMAGE TO COMPUTER SYSTEMS OR FOR LOSS OF DATA THAT RESULTS FROM THE DOWNLOAD OR USE OF ANY SUCH MATERIAL.");
        statement.add("NO ADVICE OR INFORMATION, WHETHER ORAL OR WRITTEN, OBTAINED BY YOU FROM OUR SITE OR THROUGH OR FROM THE SERVICES SHALL CREATE ANY WARRANTY NOT EXPRESSLY STATED IN THEIR TERMS.");
        results.put(Statements.createObject("Information Disclaimer and Disclaimer of Warranties", statement));

        statement = new ArrayList<String>();
        statement.add("YOU UNDERSTAND AND AGREE THAT LUSIDITY IS NOT LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, EXEMPLARY OR PUNITIVE DAMAGES, INCLUDING, BUT NOT LIMITED TO, ANY LOSS OF USE, LOSS OF PROFITS, LOSS OF DATA, LOSS OF GOODWILL, COST OF PROCUREMENT OF SUBSTITUTE SERVICES, OR ANY OTHER INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, EXEMPLARY OR PUNITIVE DAMAGES, HOWSOEVER CAUSED, AND ON ANY THEORY OF LIABILITY, WHETHER FOR BREACH OF CONTRACT, TORT (INCLUDING NEGLIGENCE AND STRICT LIABILITY), OR OTHERWISE RESULTING FROM: (1) THE USE OF, OR THE INABILITY TO USE, THE SITE OR THE SERVICES; (2) THE COST OF PROCUREMENT OF SUBSTITUTE SERVICES, GOODS OR AND WEB SITE; (3) UNAUTHORIZED ACCESS TO OR ALTERATION OF YOUR TRANSMISSIONS OR DATA; (4) THE STATEMENTS OR CONDUCT OF ANY THIRD PARTY ON OUR SITE; (5) RELIANCE ON CONTENT OR POSTINGS ON OUR SITE; OR (6) ANY OTHER MATTER RELATING TO OUR SITE OR THE SERVICES. THESE LIMITATIONS WILL APPLY WHETHER OR NOT LUSIDITY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES AND NOTWITHSTANDING ANY FAILURE OF ESSENTIAL PURPOSE OF ANY LIMITED REMEDY.");
        statement.add("SOME JURISDICTIONS DO NOT ALLOW THE LIMITATION OR EXCLUSION OF LIABILITY FOR INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, EXEMPLARY OR PUNITIVE DAMAGES, SO SOME OF THE ABOVE LIMITATIONS MAY NOT APPLY TO YOU AND OUR LIABILITY WILL BE LIMITED TO THE MAXIMUM AMOUNT PERMITTED BY LAW.");
        results.put(Statements.createObject("Limitation Of Liability", statement));

        statement = new ArrayList<String>();
        statement.add("lusidity is a registered trademark and the lusidity logo are trademarks of Venio Inc. D/B/A lusidity All rights reserved. All other trademarks and logos on the Site are the property of their respective owners.");
        results.put(Statements.createObject("Trademark Notices", statement));

        statement = new ArrayList<String>();
        statement.add("The images referenced, made accessible or made available to you on the Site or by means of the Services are protected by the copyright and trademark laws of the United States and other countries. Although you are permitted to locate and access the images through the Services, you may need to obtain authorization of the owner of such materials before using them for any purpose other than viewing on the web. For authorizations to use an image, please contact the image owner as indicated on the source site, not lusidity.environment. lusidity cannot give you authorization to use the copyrighted images. lusidity cannot guarantee that a search will not locate unintended or objectionable content and lusidity accepts no responsibility or liability for the content of any site included in any search results or otherwise linked to by the Services, or for your use of such content.");
        results.put(Statements.createObject("Images", statement));

        statement = new ArrayList<String>();
        statement.add("Financial data provided may be delayed as specified by financial exchanges or our data providers. Financial data and information is provided for informational purposes only, and is not intended for trading purposes. lusidity does not guarantee the accuracy, timeliness, reliability or completeness of any such data. Neither lusidity nor any of its data or content providers shall be liable for any errors or delays in the content, or for any actions taken in reliance thereon. You agree not to redistribute the data found herein.");
        results.put(Statements.createObject("Financial Information (Stock Quotes, Currency Conversions, Exchange Rates and such)", statement));

        statement = new ArrayList<String>();
        statement.add("lusidity adds \"affiliate codes\" to outgoing clicks to certain websites, such as web retailers, and lusidity receives a payment for doing so. lusidity is not recommending or endorsing products mentioned in these affiliated links.");
        results.put(Statements.createObject("Affiliated Links", statement));

        statement = new ArrayList<String>();
        statement.add("lusidity incorporates the Google Maps API to help you discover places around you." +
                "  <a href=\"http://www.google.com/policies/privacy/\">Google's privacy policy</a>");
        results.put(Statements.createObject("Google Maps API", statement));

        statement = new ArrayList<String>();
        statement.add("With the exception of our Privacy Policy, this Agreement constitutes the entire agreement between you and lusidity for governing your use of our Site and the Services and supersedes any prior agreements between you and lusidity for that purpose, including any membership agreements or other similar agreements applying to our Site or the Services.  This Agreement is only between you and us, and it not for the benefit of any other person (except for permitted successors and assigns under this Agreement).");
        results.put(Statements.createObject("No Other Agreements Between Us/No Third-Party Beneficiaries", statement));

        statement = new ArrayList<String>();
        statement.add("This Agreement and the relationship between you and lusidity are governed by the laws of the EntityState of Virginia without regard to its conflict of law provisions. You and lusidity agree to irrevocably submit to the personal and exclusive jurisdiction of the courts located within the county of Prince William County, Virginia. Notwithstanding the foregoing, lusidity may seek equitable and injunctive relief in any jurisdiction.");
        results.put(Statements.createObject("Law Applicable to Interpretations and Disputes", statement));

        statement = new ArrayList<String>();
        statement.add("If any provision of this Agreement is found by a court or other binding authority to be invalid, you agree that every attempt shall be made to give effect to the parties' intentions as reflected in that provision, and the remaining provisions contained in this Agreement shall continue in full force and effect.");
        results.put(Statements.createObject("Severability of This Agreement", statement));

        statement = new ArrayList<String>();
        statement.add("You agree that any claim or cause of action arising out of your use of our Site or this Agreement must be filed within one year after such claim or cause of action arose or it shall forever be barred, notwithstanding any statute of limitations or other law to the contrary. Within this period, any failure by lusidity to enforce or exercise any provision of this Agreement or related right shall not constitute a waiver of that right or provision.");
        results.put(Statements.createObject("Limitation of Actions Brought Against lusidity", statement));

        statement = new ArrayList<String>();
        statement.add("If you have any questions or concerns with respect to this Agreement or our Site, you may contact a representative of lusidity by email at admin@lusidity.com or by mail at:");
        statement.add("Venio Inc.<br \\>" +
                "Attention: Legal Department Privacy Policy Dispute Resolution<br \\>" +
                "12909 Luca Station Way Suite 200<br \\>" +
                "FAIRFAX STATION, VA 22192");
        results.put(Statements.createObject("Contact Information", statement));


        statement = new ArrayList<String>();
        statement.add("Effective April 2013");
        statement.add("It is the policy of Venio Inc., d/b/a lusidity (lusidity) to respond to clear notices of alleged copyright and trademark infringement. Regardless of whether lusidity would be found to be liable for such copyright or trademark infringement under United States law or the laws of the applicable country, we may respond to allegations of such infringement by removing or disabling access to content that is claimed to be infringing or by terminating particular subscribers ability to access the lusidity website and services. In the event that we terminate access to the lusidity website or remove search results in response to such a notice, we will make a good-faith effort to contact the site or content owners or administrators in order to permit them to make a counter-notification.");
        statement.add("Please note that, in the event you materially misrepresent that a particular material or activity is infringing your copyright or trademark, you will be liable for all damages incurred (including costs and attorneys fees) as a result of such material misrepresentation. Accordingly, we suggest that you contact an attorney before notifying lusidity of infringement if you are not certain whether particular content is infringing your copyright or trademark.");
        results.put(Statements.createObject("Infringement Policy", statement));

        statement = new ArrayList<String>();
        statement.add("If you believe that your copyright or trademark is being infringed on the lusidity website, please send us a written notice that must include the following information:");
        statement.add("An identification of the copyrighted or trademarked work that you believe has been infringed;");
        statement.add("An identification of the allegedly infringing content within the work listed in item #1 above, and sufficient information to permit lusidity to identify it on the website. For a web search result, you must identify the search results obtained that link directly to a webpage that allegedly contains infringing material. In other words, you must provide us with (a) the search query you used and (b) the web address (URL) of each search result you allege contains infringing material;");
        statement.add("A statement by you that you have a good faith belief that the use of the content identified in your notice in the manner complained of is not authorized by the copyright or trademark owner, its agent, or the law;");
        statement.add("A statement by you that you swear and attest, under penalty of perjury, that the information in your notice is accurate and that you are the copyright/trademark owner or authorized to act on the owners behalf of an exclusive right that is allegedly infringed;");
        statement.add("Your signature (in either electronic or physical form), along with your name, address, telephone number and, if available, email address.");
        results.put(Statements.createObject("Infringement Notice", statement));


        statement = new ArrayList<String>();
        statement.add("While it is our policy to respond to clear notices of trademark and copyright infringement, if you believe that your content should not have been removed for alleged copyright or trademark infringement, you may send us a written counter-notice. Such counter-notice must include the following information:");
        statement.add("An identification of the work that was removed or disabled, and the location (such as a URL) of the website on which it would have been found prior to its removal;");
        statement.add("A statement by you that you swear and attest, under penalty of perjury, that you have a good faith belief that the search result and/or content was removed or disabled as a result of a mistake or misidentification. For trademark disputes only: information reasonably sufficient to explain why you believe you are not infringing the trademarked work;");
        statement.add("A statement that you consent either to the jurisdiction of (a) the Federal District Court for the judicial district in which your address is located if you live in the United States or (b) any judicial district in which lusidity is located if you live outside the United States. Please also include a statement that you will accept service of process from the person who sent the original infringement notice to lusidity, or an agent of such person; and");
        statement.add("Your signature (in either electronic or physical form), along with your name, address, telephone number and, if available, email address.");
        statement.add("It is our policy to respond to all such notices we receive and to comply with the provisions of applicable law. lusidity reserves the right, in its sole discretion, to remove any content that is alleged to infringe any copyright or trademark without prior notice and to terminate the account of any user who lusidity has determined to be a repeat infringer.");
        statement.add("Please send or fax all infringement notices and counter-notices to the following address:");
        statement.add("Venio Inc.<br \\>" +
                "Attention: Copyright Agent<br \\>" +
                "12909 Luca Station Way, Suite 200<br \\>" +
                "FAIRFAX STATION, VA 22192<br \\>" +
                "admin@lusidity.com");
        statement.add("Note: This address is provided exclusively for notifying us that your copyrighted or trademarked material may have been infringed. Inquiries regarding any other topics, such as technical support requests, reports concerning email abuse, and reports of instances of piracy, will not receive a response through this process.");
        results.put(Statements.createObject("Counter-Notice", statement));

        return results;
    }

    /**
     * Privacy Policy
     *
     * @return Privacy Policy
     * @throws JSONException
     */
    private static JSONArray getPrivacyPolicy() throws JSONException {
        JSONArray results = new JSONArray();

        Collection<String> statement = new ArrayList<String>();
        statement.add("Venio Inc. d/b/a lusidity (\"lusidity\" or We \") is committed to respecting and protecting the " +
                "privacy of the users of our services. The following describes the information collection and use" +
                " practices for the services offered on lusidity.com (\"lusidity\") and its sub-domains.");
        statement.add("By using lusidity, you consent to the collection and use of your " +
                "information as described in these policies. In addition, lusidity may have different privacy policies " +
                "for other products and services that we " +
                "offer. Certain services and content made available through, or " +
                "accessible from lusidity may be provided by third parties. The " +
                "collection and use of your information in connection with such services and content may be governed by " +
                "the privacy policies, terms of use, or other terms and conditions of such third parties.");
        results.put(Statements.createObject("Privacy Commitment", statement));

        statement = new ArrayList<String>();
        statement.add("No personally identifiable information is ever required by lusidity.environment. This means lusidity never " +
                "seeks any information related to your name, telephone number, address, or even your email address " +
                "unless you request a lusidity Service where that information is required. lisidity is intended to " +
                "be an anonymous service. We do collect limited non-personally identifying information that your " +
                "browser makes available. This log information includes your Internet Protocol address, browser " +
                "type, browser language, referral data, the date and time of your " +
                "query, the type of device you are accessing the Service with, and one or more cookies " +
                "(described below) that may uniquely identify your browser. International users (defined as " +
                "those outside the continental United States of America) are subject to tracking, cookies and " +
                "other government required protocols as required by the appropriate US Government Agencies such " +
                "as the FBI, NSA or CIA. lusidity will not track a US citizen for any reason, unless such information " +
                "is required to comply with court orders or subpoenas or when required to do so by law. We use this " +
                "information to operate, develop and improve our services. When we require personally identifying " +
                "information (such as but not limited to an email for the “sign in” feature colected from your " +
                "authentication provider) they will inform you about the types of information we collect.");
        results.put(Statements.createObject("Data Collection (Absence of Personally Identifiable Information)", statement));

        statement = new ArrayList<String>();
        statement.add("We do not use cookies to track your behavior nor do we sell any information to third parties." +
                "  We do use cookies to improve the delivery of the lusidity service and identify returning" +
                " lusidity account holders if you choose at any time to create a lusidity account. " +
                "A “cookie” is a packet of information sent by a server to a Web browser and then " +
                "sent back by the browser each time it accesses that server, or to cache user preferences " +
                "(such as but not limited to user selected graphics). We use cookies only to store personalization " +
                "and sign in settings and to optimize the speed of lusidity.environment. By default most browsers are set up to " +
                "accept cookies. You can set your browser to refuse all cookies; however, after which some lusidity " +
                "features may not function properly.");
        results.put(Statements.createObject("Cookies", statement));

        statement = new ArrayList<String>();
        statement.add("We use the information collected from you to improve the Search Experience and our other " +
                "services. We may also share the non-personally identifiable information that we collect with our " +
                "business partners or clients so that they may improve their services, which may be made available " +
                "to you through lusidity.environment. lusidity does not sell or provide personally identifiable information to " +
                "any third parties. However, we may disclose personally identifying information to comply with court " +
                "orders or subpoenas or when required to do so by law.");
        results.put(Statements.createObject("Use of Collected Information", statement));

        statement = new ArrayList<String>();
        statement.add("The privacy policy of sites displayed as search results, or otherwise linked to by " +
                "lusidity, is not controlled by lusidity.environment. These other sites may place their own cookies on your " +
                "computer, collect data or solicit personal information.");
        results.put(Statements.createObject("Links", statement));

        statement = new ArrayList<String>();
        statement.add("lusidity constantly strives to provide the best service. As new products and changes to " +
                "existing products are made, lusidity reserves the right to amend this Privacy Policy at any time." +
                " Any changes to this Policy will be posted on this page so that you are always aware of the current" +
                " information collection and usage practices associated with your use of lusidity.environment. Your use of" +
                " lusidity is also governed by the Terms of Service.");
        results.put(Statements.createObject("Policy Changes", statement));

        statement = new ArrayList<String>();
        statement.add("If you have any comments or questions about your privacy, please contact admin@ylusidity.com");
        results.put(Statements.createObject("More Information", statement));

        statement = new ArrayList<String>();
        statement.add("All contents of this Web site are: Copyright ©2013 Venio Inc. All rights reserved.");
        statement.add("The lusidity and associated Marks that appear throughout lusidity belong to Venio Inc." +
                " and are protected by trademark laws. To seek permission to use any of the lusidity Marks, please contact us at admin@lusidity.com");
        results.put(Statements.createObject("Copyright and Trademark", statement));

        return results;
    }

    /**
     * Creates a JSONObject
     *
     * @param title      The title property value.
     * @param paragraphs The summary/description paragraphs.
     * @return a JSONObject
     * @throws JSONException
     */
    private static JSONObject createObject(String title, Collection<String> paragraphs) throws JSONException {
        JSONObject result = new JSONObject();

        if (!StringX.isBlank(title)) {
            result.put("title", title);
        }

        JSONArray statements = new JSONArray(paragraphs);
        result.put("statements", statements);

        return result;
    }

}
