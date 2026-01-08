# auto-cve
lightweight utility designed to simplify Talend CVE remediation.

---

# Auto-CVE Tool User Guide

`auto-cve` is an automation utility designed for Talend development workflows. It streamlines CVE (Common Vulnerabilities and Exposures) scanning, dependency management, and routine Git operations.

## 1. Key Features

### A. Component Security Scanning

* **Targeted Scanning**: Scans specific JAR files within the Talend Repository to identify affected components and dependencies.

### B. Test Automation Support

* **List Generation**: Automatically generates **TUJ (Talend User Job) folder lists** and **API Test Group lists**.
* **Jenkins Integration**: Outputs are formatted for direct use as parameters in Jenkins jobs.

### C. Developer Productivity Tools

* **Code Generation**: Automatically generates Maven `Exclude` tags or `dependencyManagement` blocks.
* **Bulk Git Operations**:
* Batch branch switching and bulk commits.
* Automatic PR (Pull Request) link creation.
* Cherry-picking from `master` across multiple repositories.


* **Patch Management**: Generates patches specifically for testing purposes.

---

## 2. How to Use Auto-CVE

### Step 1: Clone Repositories

Run the `clone.bat` script located in the root directory to clone all required repositories to your local machine.

### Step 2: Configuration

1. Navigate to `src/main/resources/`.
2. Locate the `config-template.properties` file.
3. **Update Settings**: Fill in your local environment details and Jira information.
4. **Rename**: Save the modified file as `config.properties`.

### Step 3: Execution

1. Open the `auto-cve` project in IntelliJ IDEA or your preferred IDE.
2. Locate the `RunAll` class and run it.

### Step 4: Verify Results

* **Check Logs**: Go to the `result_folder` defined in your `config.properties`.
* **Scanning Report**: You can find the list of CVE components in `forest_folder/cve_jar_artifactId/search_result.log`.
* **Automatic Branching**: The tool will automatically create a new branch named `git_your_name/jira_id_jarName` in every relevant repository.

### Step 5: Using Utility Classes

* **AutoTestUtil**: Use this class to quickly generate the TUJ folder strings (for TTPv2) and test group strings (for API testing) needed for your verification tasks.


---

# Auto-CVE 工具使用指南

`auto-cve` 是一个专为 Talend 开发流程设计的自动化工具，旨在简化 CVE 漏洞修复、组件扫描及日常 Git 维护操作。

## 1. 主要功能

### A. 组件安全扫描

* **定向扫描**：针对 Talend Repository 中的特定 JAR 包进行组件依赖扫描，快速识别受影响范围。

### B. 测试自动化支持

* **生成测试列表**：自动生成涉及到的 **TUJ (Talend User Job) 文件夹列表**和 **API Test Group 列表**。
* **Jenkins 友好**：生成的字符串可以直接粘贴到 Jenkins 参数中使用。

### C. 开发实用小工具

* **代码生成**：自动生成 Maven 的 `Exclude` 或 `dependencyManagement` 配置代码。
* **Git 批量操作**：
* 批量切换分支（Switch Branch）。
* 批量提交代码（Commit）并创建 PR 链接。
* 快速从 master 分支进行 Cherry-pick。


* **补丁管理**：快速生成用于测试的 Patch 文件。

---

## 2. 使用步骤

### 第一步：准备代码库

运行项目根目录下的 `clone.bat` 脚本，克隆所有相关的 Repository 到本地。

### 第二步：配置环境

1. 进入目录：`src/main/resources/`。
2. 找到模板文件：`config-template.properties`。
3. **修改配置**：根据你的本地路径和 Jira 信息填写配置。
4. **重命名**：将修改后的文件另存为 `config.properties`。

### 第三步：运行扫描

1. 使用 IntelliJ IDEA 或其他 IDE 打开 `auto-cve` 项目。
2. 找到 `RunAll` 类并执行 `main` 方法。

### 第四步：查看结果

* **查看报告**：前往你在 `config.properties` 中设置的 `result_folder`。
* **日志路径**：在 `forest_folder/cve_jar_artifactId/search_result.log` 中查看该 JAR 涉及的 CVE 组件列表。
* **自动分支**：程序会自动在涉及到的 Repository 中创建名为 `git_your_name/jira_id_jarName` 的新分支。

### 第五步：辅助工具使用

* **AutoTestUtil**：如果你需要运行测试，可以使用该工具类快速生成 TTPv2 运行所需的 TUJ folder 字符串和 API Test 的 test group 字符串。

---
