package com.development.forge.server.task

object TaskRepository {

    private val tasks = mutableListOf(
        Task("cleaning", "Clean the house", Priority.Low),
        Task("gardening", "Mow the lawn", Priority.Medium),
        Task("shopping", "Buy the groceries", Priority.High),
        Task("painting", "Paint the fence", Priority.Medium)
    )

    fun allTasks() = tasks.toList()

    fun taskByPriority(priority: Priority) = tasks.filter { it.priority == priority }

    private fun taskByName(name: String) = tasks.find { it.name.equals(name, ignoreCase = true) }

    fun addTask(task: Task) =
        if (taskByName(task.name) != null)
            throw IllegalStateException("duplicate task cannot be added")
        else tasks.add(task)
}