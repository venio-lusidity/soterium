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

package com.lusidity.athena.console.commands;


import com.lusidity.console.Console;
import com.lusidity.console.commands.BaseCommand;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.people.Person;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.json.JsonData;
import com.lusidity.security.IdentityHelper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

@SuppressWarnings("UnusedDeclaration")
public
class ApiKeyCommand
	extends BaseCommand
{
// -------------------------- OTHER METHODS --------------------------

    private static final String PRINCIPAL_OPTION = "principal";
    private static final String PROVIDER_OPTION = "provider";
    private static final String VALUE_OPTION = "value";

	// Constructors
	public ApiKeyCommand(){
		super();
	}

	// Overrides
	@Override
	public void execute(Console console, CommandLine commandLine)
		throws Exception
	{
		// apiKey -principal /domains/people_person/a10c5eea653247878caa389570cce3fd -provider x509 -value rmk.disa.mil

		String principalId = commandLine.getOptionValue(ApiKeyCommand.PRINCIPAL_OPTION);
		String provider = commandLine.getOptionValue(ApiKeyCommand.PROVIDER_OPTION);
		String value = commandLine.getOptionValue(ApiKeyCommand.VALUE_OPTION);

		Person person =VertexFactory.getInstance().get(Person.class, principalId);
		if(null!=person){
			String apiKey = Identity.composeKey(provider, value);
			Identity identity = IdentityHelper.getOrCreateApiIdentity(person, apiKey, provider, value);
			if(null==identity){
				System.out.println("Failed to create the API key and or associate to a principal.");
			}
			else{
				System.out.println(String.format("API Key associated to the principal. %s", apiKey));
			}
		}
		else{
			System.out.println("A valid principal was not found.");
		}
	}

	@Override
	public JsonData execute(JsonData params, BasePrincipal principal)
	{
		return null;
	}

	@Override
	public
	String getDescription()
	{
		return "Obfuscate something.";
	}

	/**
	 * Get name for this command.
	 *
	 * @return Command name.
	 */
	@Override
	public
	String getName()
	{
		return "apiKey";
	}

	/**
	 * Get command line options for this command.
	 *
	 * @return Apache Commons CLI Options.
	 */
	@Override
	public
	Options getOptions()
	{
        Options options = new Options();

        Option seed = new Option(ApiKeyCommand.PRINCIPAL_OPTION, true, "The principal to register the API key with.");
        seed.setRequired(true);
        seed.setType(String.class);
        options.addOption(seed);

        Option provider = new Option(ApiKeyCommand.PROVIDER_OPTION, true, "The source of the value/identity, apiKey.");
        provider.setRequired(true);
        provider.setType(String.class);
        options.addOption(provider);

        Option value = new Option(ApiKeyCommand.VALUE_OPTION, true, "The value being obfuscated.");
        value.setRequired(true);
        value.setType(String.class);
        options.addOption(value);

        return options;
	}

    @Override
    public boolean isWebEnabled() {
        return false;
    }

}
