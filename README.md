# 🌟 Spring AI Telegram Task Manager

This is a Chatbot that uses OpenAI client with GPT-4o-mini in Telegram that allows you to manage tasks by writing. 

## 🚀 Key Functionalities

* **Task management**: The bot allows users to create, update, and delete tasks using natural language commands. It converts these commands into SQL queries to manage tasks in the database.
* **User interaction**: The bot interacts with users through Telegram, allowing them to send messages and receive responses. It can handle new user registrations and verify their email addresses.
* **Task assignment**: Users can assign tasks to themselves or others, and the bot updates the task assignee in the database.
* **Task status updates**: Users can change the status of tasks to "TODO", "IN_PROGRESS", "BLOCKED", or "DONE".
* **Notifications**: The bot sends notifications to all users when a task is created, updated, or deleted.
* **Help and welcome messages**: The bot provides help and welcome messages to guide users on how to use its functionalities.

These functionalities are implemented in various classes such as `ManagerAgent` (`src/main/java/com/fmoreno/telegramtaskaiagent/agents/ManagerAgent.java`), `NL2SQLAgent` (`src/main/java/com/fmoreno/telegramtaskaiagent/agents/NL2SQLAgent.java`), `NotificationAgent` (`src/main/java/com/fmoreno/telegramtaskaiagent/agents/NotificationAgent.java`), and `TelegramClientConsumer` (`src/main/java/com/fmoreno/telegramtaskaiagent/client/TelegramClientConsumer.java`). The bot uses the OpenAI API for natural language processing and interacts with a database to manage tasks. The configuration files and dependencies are defined in `pom.xml` and other configuration files. The bot also uses environment variables for configuration, as mentioned in the `README.md`.

## 🛠️ Prerequisites

To run the Spring AI Telegram Task Manager bot, you need to ensure the following prerequisites are met:

* **Java Development Kit (JDK)**: Ensure you have JDK 17 installed on your system.
* **Maven**: Make sure you have Apache Maven installed to manage the project's dependencies and build the application.
* **OpenAI API Key**: Obtain an OpenAI API key to enable the bot to use the OpenAI API for natural language processing.
* **Telegram Bot Token**: Create a Telegram bot and obtain the bot token to allow the bot to interact with users on Telegram.
* **Environment Variables**: Set up the required environment variables in a `.env` file located in the `src/main/resources` directory. The required environment variables are:
  * `SPRING_AI_OPENAI_API_KEY`: Your OpenAI API key.
  * `TELEGRAM_TOKEN`: Your Telegram bot token.
  * `ALLOWED_EMAILS`: A comma-separated list of allowed email addresses.

For more details on setting up the environment variables, refer to the `src/main/resources/.env` file. Additionally, ensure that the dependencies listed in the `pom.xml` file are properly configured. The application will load these environment variables using the `Dotenv` library, as configured in `src/main/java/com/fmoreno/telegramtaskaiagent/config/DotEnvInitializer.java`.

## 🌐 Setting Up Environment Variables

To set up the environment variables for the Spring AI Telegram Task Manager, follow these steps:

* Create a file named `.env` in the `src/main/resources` directory if it doesn't already exist.
* Add the following environment variables to the `.env` file:
  * `SPRING_AI_OPENAI_API_KEY`: Your OpenAI API key.
  * `TELEGRAM_TOKEN`: Your Telegram bot token.
  * `ALLOWED_EMAILS`: A comma-separated list of allowed email addresses.

Here is an example of what the `.env` file should look like:

```
SPRING_AI_OPENAI_API_KEY=(your openai api key)
TELEGRAM_TOKEN=(your telegram bot token)
ALLOWED_EMAILS=allowed1@example.com,allowed2@example.com,allowed3@example.com
```

The application will load these environment variables using the `Dotenv` library, as configured in `src/main/java/com/fmoreno/telegramtaskaiagent/config/DotEnvInitializer.java`. Make sure to replace the placeholder values with your actual API key, bot token, and allowed email addresses.

## 📧 Allowed Emails

The `ALLOWED_EMAILS` environment variable is used to specify a list of email addresses that are permitted to interact with the Spring AI Telegram Task Manager bot. Here are the key points about the `ALLOWED_EMAILS`:

* **Purpose**: The `ALLOWED_EMAILS` variable is used to restrict access to the bot, ensuring that only authorized users can interact with it.
* **Configuration**: The `ALLOWED_EMAILS` variable is set in the `.env` file located in the `src/main/resources` directory. It contains a comma-separated list of allowed email addresses.
* **Usage**: When a new user sends a message to the bot, the bot extracts the email address from the message and checks if it is in the list of allowed emails. If the email is allowed, the user is added to the system and can interact with the bot. If the email is not allowed, the user is informed that their email is not permitted.
* **Implementation**: The `AllowedEmailsConfig` class (`src/main/java/com/fmoreno/telegramtaskaiagent/config/AllowedEmailsConfig.java`) loads the `ALLOWED_EMAILS` environment variable and splits it into a list of allowed email addresses. The `TelegramClientConsumer` class (`src/main/java/com/fmoreno/telegramtaskaiagent/client/TelegramClientConsumer.java`) uses this list to verify if a user's email is allowed.

This mechanism ensures that only users with authorized email addresses can use the bot, providing a layer of security and control over who can interact with the task management system.
