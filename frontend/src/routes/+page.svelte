<script lang="ts">
  import { TabItem, Tabs } from "flowbite-svelte";

  import StatusTable from "../modules/StatusTable/StatusTable.svelte";
  import SettingsWidget from "../modules/SettingsWidget/SettingsWidget.svelte";
  import DashboardIcon from "../icons/DashboardIcon.svelte";
  import TopicsIcon from "../icons/TopicsIcon.svelte";
  import Topics from "../modules/Topics/Topics.svelte";
  import StatusBar from "../modules/StatusBar/StatusBar.svelte";
</script>

<h1 class="text-6xl">Welcome to RoboChief</h1>
<StatusBar />

<Tabs style="underline">
    <TabItem open>
        <div slot="title" class="flex items-center gap-2">
            <DashboardIcon class="h-5 w-auto" />
            Dashboard
        </div>
        <div class="grid gap-2">
            <SettingsWidget />
            <StatusTable />
        </div>
    </TabItem>

    <TabItem>
        <div slot="title" class="flex items-center gap-2">
            <TopicsIcon class="h-5 w-auto" />
            Topics
        </div>
        <div class="grid gap-2">
            <Topics />
        </div>
    </TabItem>
</Tabs>
