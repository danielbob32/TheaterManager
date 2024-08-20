# 🎬 Movie Theater Management System

## 📖 Overview

This project is a comprehensive Movie Theater Management System developed using Java, JavaFX, and Hibernate. It's designed to manage multiple aspects of a movie theater chain, including ticket sales, movie schedules, customer management, reporting and more.

## ✨ Features

- 🔐 **User Authentication**: Separate login systems for customers and staff members.
- 🎥 **Movie Management**: Add, update, and delete movies. Manage movie details including title, description, duration, and genre.
- 📅 **Screening Management**: Schedule movie screenings across different theaters and halls.
- 🎟️ **Ticket Booking**: Allow customers to book tickets for movie screenings, including seat selection.
- 🏠 **Home Movie Links**: Provide options for customers to purchase links for home viewing of selected movies.
- 📚 **Ticket Tabs**: Implement a system for bulk ticket purchases (ticket tabs).
- 💰 **Dynamic Pricing**: Support for changing movie prices with approval system.
- 📊 **Reporting**: Generate various reports including sales data and customer complaints.
- 📞 **Customer Complaint System**: Handle and track customer complaints.

## 🛠️ Technology Stack

- 🧠 **Backend**: Java
- 🖥️ **Frontend**: JavaFX
- 🗄️ **Database**: MySQL with Hibernate ORM
- 🏗️ **Build Tool**: Maven
- 🔄 **Version Control**: Git

## 🏗️ Project Structure

The project is divided into two main components:

1. 🖥️ **Client**: Handles the user interface and client-side logic.
2. 🖧 **Server**: Manages the business logic and database interactions.

Key packages include:
- `entities`: Contains all entity classes (Movie, Screening, Ticket, etc.)
- `client`: Client-side controllers and views
- `server`: Server-side logic and database operations

## 🚀 Setup and Installation

1. Clone the repository
2. Ensure you have Java JDK 13 or later installed
3. Set up MySQL database and update `hibernate.properties` with your database credentials
4. Build the project using Maven: `mvn clean install`
5. Run the server application
6. Run the client application

<p align="center">
  Made with by Yarden Itzhaky, Daniel Bobritzki, Doston Islambekov, Meshi Itzhaki, Yonatan Harel.
</p>
