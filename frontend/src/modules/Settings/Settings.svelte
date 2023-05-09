<script lang="ts">
  import { Button, Table, TableBody, TableBodyCell, TableBodyRow, TableHead, TableHeadCell } from "flowbite-svelte";

  import { getAllSettingsAndValues, Setting, Type } from "./Settings";
  import { execute } from "$lib/actions.js";
  import { onDestroy, onMount } from "svelte";

  let settings = Array.from(getAllSettingsAndValues().entries());

  const getType = (type: Type) => {
    switch (type) {
      case Type.String:
        return "String";
      case Type.Boolean:
        return "Boolean";
      case Type.Number:
        return "Number";
    }
  };

  const getStatus = (setting: Setting<any>, value: string) => {
    if (setting.default === value) {
      return "default";
    }

    return "user set";
  };

  let update: number | undefined;
  onMount(() => setInterval(() => {
    settings = Array.from(getAllSettingsAndValues().entries());
  }, 1000));
  onDestroy(() => clearInterval(update!));
</script>

<Table>
    <TableHead>
        <TableHeadCell>Key</TableHeadCell>
        <TableHeadCell>Status</TableHeadCell>
        <TableHeadCell>Type</TableHeadCell>
        <TableHeadCell>Value</TableHeadCell>
    </TableHead>
    <TableBody>
        {#each settings as [setting, value]}
            <TableBodyRow>
                <TableBodyCell>{setting.key}</TableBodyCell>
                <TableBodyCell>{getStatus(setting, value)}</TableBodyCell>
                <TableBodyCell>{getType(setting.type)}</TableBodyCell>
                <TableBodyCell>{value}</TableBodyCell>
            </TableBodyRow>
        {/each}
    </TableBody>
    <tfoot>
    <tr>
        <td class="py-3 px-6"></td>
        <td class="py-3 px-6"></td>
        <td class="py-3 px-6"></td>
        <td class="py-3 px-6">
            <Button on:click={() => execute({ actionUrl: "/actions/modal?resource=set_setting" })}>
                Add
            </Button>
        </td>
    </tr>
    </tfoot>
</Table>
