class TaskManager(val maxCount: Int = 42) {
    var tasks: List<Task> = listOf();
    private val lock = Any();
    
    @Synchronized fun add(task: Task) {
        if(isMaxCountReached()) return;
    	tasks += task; 
    }
    
    @Synchronized fun addFiFo(task: Task) {
        if(!isMaxCountReached()) {
            tasks += task;
            return;
        }
        
        val (firstTask) = tasks;
        tasks = tasks.drop(1) + task;
        firstTask.kill();
    }
    
    @Synchronized fun addPriorityBased(task: Task) {
        if(!isMaxCountReached()) {
            tasks += task;
            return;
        }
        
        if(task.priority == Priority.LOW) return;
        
        val firstLowPriorityTask = tasks.firstOrNull{it.priority == Priority.LOW};
        if(firstLowPriorityTask != null) {
            replaceTask(firstLowPriorityTask, task);
            return;
        }
        
        if(task.priority == Priority.MEDIUM) return;
        
        val firstMediumPriorityTask = tasks.firstOrNull{it.priority == Priority.MEDIUM};
        if(firstMediumPriorityTask != null) {
            replaceTask(firstMediumPriorityTask, task);
            return;
        }
    }
    
    private fun replaceTask(taskToKill: Task, taskToAdd: Task) {
        tasks = tasks.filter{it.pid != taskToKill.pid} + taskToAdd;
        taskToKill.kill();
    }
    
    private fun isMaxCountReached(): Boolean {
        return tasks.size == maxCount;
    }
    
    fun list(): List<Task> {
        return tasks;
    }
    
    fun listByPid(): List<Task> {
        return tasks.sortedBy{ it.pid };
    }
    
    fun listByPriority(): List<Task> {
        return tasks.sortedBy{ it.priority };
    }
    
    @Synchronized fun kill(pid: Int) {
        kill{it.pid == pid};
    }
    
    @Synchronized fun killGroup(priority: Priority) {
        kill{it.priority == priority};
    }
    
    @Synchronized fun killAll() {
        tasks.forEach{it.kill()};
        tasks = listOf();
    }
    
    private fun kill(predicate: (Task) -> Boolean) {
        tasks.filter(predicate).forEach{it.kill()};
        tasks = tasks.filter{!predicate(it)};
    }
}
